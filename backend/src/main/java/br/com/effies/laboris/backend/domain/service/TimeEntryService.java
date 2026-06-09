package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.JobAssignmentId;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.JobStatus;
import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import br.com.effies.laboris.backend.domain.entity.enums.UserStatus;
import br.com.effies.laboris.backend.domain.repository.JobAssignmentRepository;
import br.com.effies.laboris.backend.domain.repository.DisplacementRepository;
import br.com.effies.laboris.backend.domain.repository.JobRepository;
import br.com.effies.laboris.backend.domain.repository.TimeEntryRepository;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.presentation.dto.request.TimeEntryRequestDto;
import br.com.effies.laboris.backend.presentation.dto.request.ManagerTimeEntryRequestDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final JobRepository jobRepository;
    private final JobAssignmentRepository assignmentRepository;
    private final DisplacementRepository displacementRepository;
    private final UserRepository userRepository;

    public TimeEntryService(
        TimeEntryRepository entryRepository,
        JobRepository jobRepository,
        JobAssignmentRepository assignmentRepository,
        DisplacementRepository displacementRepository,
        UserRepository userRepository
    ){
        this.timeEntryRepository = entryRepository;
        this.jobRepository = jobRepository;
        this.assignmentRepository = assignmentRepository;
        this.displacementRepository = displacementRepository;
        this.userRepository = userRepository;
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

        if (request.getEntryType() == TimeEntryType.IN) {
            if (displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId()).isPresent()) {
                throw new IllegalStateException("Não é possível bater ponto de entrada enquanto houver um deslocamento ativo em andamento. Finalize o deslocamento primeiro.");
            }
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

    public List<TimeEntry> findTimeEntriesForManager(UUID employeeId, Instant start, Instant end, User manager) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado."));

        if (!employee.getCompany().getId().equals(manager.getCompany().getId())) {
            throw new SecurityException("Acesso negado. O funcionário não pertence à sua empresa.");
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("A data de início não pode ser posterior à data final.");
        }

        return timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(
                employeeId, start, end
        );
    }

    public TimeEntry createTimeEntryForManager(UUID employeeId, ManagerTimeEntryRequestDto request, User manager) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado."));

        if (!employee.getCompany().getId().equals(manager.getCompany().getId())) {
            throw new SecurityException("Acesso negado. O funcionário não pertence à sua empresa.");
        }

        if (employee.getStatus() == UserStatus.INACTIVE) {
            throw new IllegalStateException("Não é permitido alterar pontos de funcionários inativos.");
        }

        var job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new EntityNotFoundException("Trabalho não encontrado."));

        if (!job.getCompany().getId().equals(manager.getCompany().getId())) {
            throw new SecurityException("Acesso negado. O trabalho não pertence à sua empresa.");
        }

        if (request.getTimestamp().isAfter(Instant.now())) {
            throw new IllegalArgumentException("A data e hora do ponto não podem ser no futuro.");
        }

        TimeEntry entry = new TimeEntry();
        entry.setEmployee(employee);
        entry.setJob(job);
        entry.setEntryType(request.getEntryType());
        entry.setLatitude(job.getLatitude() != null ? job.getLatitude() : 0.0);
        entry.setLongitude(job.getLongitude() != null ? job.getLongitude() : 0.0);
        entry.setManual(true);
        entry.setEntryTimestamp(request.getTimestamp());
        entry.setJustification(request.getJustification());

        return timeEntryRepository.save(entry);
    }

    public TimeEntry updateTimeEntryForManager(UUID employeeId, UUID timeEntryId, ManagerTimeEntryRequestDto request, User manager) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado."));

        if (!employee.getCompany().getId().equals(manager.getCompany().getId())) {
            throw new SecurityException("Acesso negado. O funcionário não pertence à sua empresa.");
        }

        if (employee.getStatus() == UserStatus.INACTIVE) {
            throw new IllegalStateException("Não é permitido alterar pontos de funcionários inativos.");
        }

        TimeEntry entry = timeEntryRepository.findById(timeEntryId)
                .orElseThrow(() -> new EntityNotFoundException("Registro de ponto não encontrado."));

        if (!entry.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("O registro de ponto não pertence a este funcionário.");
        }

        var job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new EntityNotFoundException("Trabalho não encontrado."));

        if (!job.getCompany().getId().equals(manager.getCompany().getId())) {
            throw new SecurityException("Acesso negado. O trabalho não pertence à sua empresa.");
        }

        if (request.getTimestamp().isAfter(Instant.now())) {
            throw new IllegalArgumentException("A data e hora do ponto não podem ser no futuro.");
        }

        entry.setJob(job);
        entry.setLatitude(job.getLatitude() != null ? job.getLatitude() : 0.0);
        entry.setLongitude(job.getLongitude() != null ? job.getLongitude() : 0.0);
        entry.setEntryType(request.getEntryType());
        entry.setEntryTimestamp(request.getTimestamp());
        entry.setJustification(request.getJustification());
        entry.setManual(true);

        return timeEntryRepository.save(entry);
    }

    public void deleteTimeEntryForManager(UUID employeeId, UUID timeEntryId, User manager) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado."));

        if (!employee.getCompany().getId().equals(manager.getCompany().getId())) {
            throw new SecurityException("Acesso negado. O funcionário não pertence à sua empresa.");
        }

        if (employee.getStatus() == UserStatus.INACTIVE) {
            throw new IllegalStateException("Não é permitido alterar pontos de funcionários inativos.");
        }

        TimeEntry entry = timeEntryRepository.findById(timeEntryId)
                .orElseThrow(() -> new EntityNotFoundException("Registro de ponto não encontrado."));

        if (!entry.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("O registro de ponto não pertence a este funcionário.");
        }

        timeEntryRepository.delete(entry);
    }
}
