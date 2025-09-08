package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.JobAssignmentId;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.JobStatus;
import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import br.com.effies.laboris.backend.domain.repository.JobAssignmentRepository;
import br.com.effies.laboris.backend.domain.repository.JobRepository;
import br.com.effies.laboris.backend.domain.repository.TimeEntryRepository;
import br.com.effies.laboris.backend.presentation.dto.request.TimeEntryRequestDto;
import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final JobRepository jobRepository;
    private final JobAssignmentRepository assignmentRepository;

    public TimeEntryService(
        TimeEntryRepository entryRepository,
        JobRepository jobRepository,
        JobAssignmentRepository assignmentRepository
    ){
        this.timeEntryRepository = entryRepository;
        this.jobRepository = jobRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public TimeEntry create (TimeEntryRequestDto request, User employee) {

        JobAssignmentId assignmentId = new JobAssignmentId();
        assignmentId.setJobId(request.getJobId());
        assignmentId.setUserId(employee.getId());

        assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new SecurityException("Acesso negado. Você não está designado para este trabalho."));

        var job = jobRepository.findById(request.getJobId())
            .orElseThrow(() -> new EntityNotFoundException("Trabalho não encontrado."));

        if(job.getStatus() != JobStatus.IN_PROGRESS){
            throw new IllegalStateException("Só é possível bater o ponto em trabalhos com status 'IN_PROGRESS'.");
        }

        Optional<TimeEntry> lastTimeEntry = timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId());

        if(request.isManual()){
            validateRulesForManualEntry(request, lastTimeEntry);
        }

        validateStateTransition(request.getEntryType(), lastTimeEntry);

        TimeEntry newTimeEntry = new TimeEntry();
        newTimeEntry.setEmployee(employee);
        newTimeEntry.setJob(job);
        newTimeEntry.setEntryType(request.getEntryType());
        newTimeEntry.setLatitude(request.getLatitude());
        newTimeEntry.setLongitude(request.getLongitude());
        newTimeEntry.setManual(request.isManual());

        if(request.isManual()){
            newTimeEntry.setEntryTimestamp(request.getReportedTimestamp());
            newTimeEntry.setJustification(request.getJustification());
        }else{
            newTimeEntry.setEntryTimestamp(Instant.now());
        }

        return timeEntryRepository.save(newTimeEntry);
    }

    public List<TimeEntry> findAllByEmployeeAndPeriod(User employee, Instant start, Instant end){

        if(start.isAfter(end)){
            throw new IllegalArgumentException("A data de início não pode ser posterior à data final.");
        }

        return timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(
            employee.getId(),
            start,
            end
        );
    }

    private void validateRulesForManualEntry(TimeEntryRequestDto request, Optional<TimeEntry> lastTimeEntry) {

        if(request.getReportedTimestamp() == null){
            throw new IllegalStateException("É preciso informar uma data e hora.");
        }

        if(lastTimeEntry.isPresent() && !request.getReportedTimestamp().isAfter(lastTimeEntry.get().getEntryTimestamp())){
            throw  new IllegalStateException("A batida de ponto manual não pode ser anterior ou igual à última batida registrada.");
        }

        if( request.getJustification() == null || request.getJustification().isBlank()){
            throw new IllegalStateException("É preciso informar o motivo da batida manual.");
        }
    }

    private void validateStateTransition(TimeEntryType nextType, Optional<TimeEntry> lastTimeEntry){

        if(lastTimeEntry.isEmpty()){
            if(nextType != TimeEntryType.CLOCK_IN){
                throw new IllegalStateException("A primeira batida de ponto do dia deve ser 'CLOCK_IN'.");
            }
            return;
        }

        TimeEntryType lastType = lastTimeEntry.get().getEntryType();

        String exceptionMessage = null;

        switch (nextType){
            case CLOCK_IN :
                if(lastType != TimeEntryType.CLOCK_OUT){
                    exceptionMessage = "Não é possível fazer 'CLOCK_IN' sem antes ter feito 'CLOCK_OUT'";
                }
                break;
            case START_BREAK:
                if(lastType != TimeEntryType.CLOCK_IN && lastType != TimeEntryType.END_BREAK){
                    exceptionMessage = "Só é possível iniciar um intervalo se você estiver trabalhando.";
                }
                break;
            case END_BREAK:
                if(lastType != TimeEntryType.START_BREAK ){
                    exceptionMessage = "Não é possível terminar um intervalo sem antes tê-lo iniciado.";
                }
                break;
            case CLOCK_OUT:
                if(lastType == TimeEntryType.CLOCK_OUT || lastType == TimeEntryType.START_BREAK ){
                    exceptionMessage = "Não é possível fazer 'CLOCK_OUT' neste momento.";
                }
                break;
        }

        if(exceptionMessage != null){
            throw new IllegalStateException(exceptionMessage);
        }
    }
}
