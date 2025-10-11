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
import org.springframework.stereotype.Service;

import java.time.Duration;
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

        if (lastTimeEntry.isPresent()) {
            Instant lastTimestamp = lastTimeEntry.get().getEntryTimestamp();
            long secondsSinceLastPunch = Duration.between( lastTimestamp, Instant.now()).getSeconds();
            if( secondsSinceLastPunch < 60 ) throw new IllegalStateException("A batida de ponto não pode ser igual à última batida registrada.");
        }

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

    public Optional<TimeEntry> findLastByEmployee(User employee){
        return timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId());
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

    private  void validateStateTransition(TimeEntryType nextType, Optional<TimeEntry> lastEntry){

        if(lastEntry.isEmpty() && nextType != TimeEntryType.IN) throw new IllegalStateException("A primeira batida de ponto deve ser uma entrada (IN).");

        if(lastEntry.isPresent()){
            TimeEntryType lastType = lastEntry.get().getEntryType();
            if( lastType == nextType) throw new IllegalStateException("Ação inválida. A última batida já foi do tipo '" + lastType + "'.");
        }
    }
}
