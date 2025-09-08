package br.com.effies.laboris.backend.domain.service;


import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.JobAssignment;
import br.com.effies.laboris.backend.domain.entity.JobAssignmentId;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.JobStatus;
import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import br.com.effies.laboris.backend.domain.repository.JobAssignmentRepository;
import br.com.effies.laboris.backend.domain.repository.JobRepository;
import br.com.effies.laboris.backend.domain.repository.TimeEntryRepository;
import br.com.effies.laboris.backend.presentation.dto.request.TimeEntryRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TimeEntryServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobAssignmentRepository jobAssignmentRepository;

    @InjectMocks
    private TimeEntryService timeEntryService;

    // Tests about TimeEntryType

    @Test
    @DisplayName("Should allow CLOCK_IN when there is no registers before.")
    void create_WithNoPreviousEntriesAndClockIn_shouldSucceed(){

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setJobId(job.getId());
        request.setEntryType(TimeEntryType.CLOCK_IN);
        request.setLatitude(0.0);
        request.setLongitude(0.0);
        request.setManual(false);

        when(jobAssignmentRepository.findById(any(JobAssignmentId.class)))
            .thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(job.getId()))
            .thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
            .thenReturn(Optional.empty());
        when(timeEntryRepository.save(any(TimeEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TimeEntry result = timeEntryService.create(request, employee);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEntryType()).isEqualTo(TimeEntryType.CLOCK_IN);
        assertThat(result.getEmployee()).isEqualTo(employee);

        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    @Test
    @DisplayName("Should allow START_BREAK when last entry is CLOCK_IN")
    void create_WhenLastEntryIsClocInAndNextIsStartBreak_ShouldSucceed(){

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setJobId(job.getId());
        request.setEntryType(TimeEntryType.START_BREAK);
        request.setLatitude(0.0);
        request.setLongitude(0.0);
        request.setManual(false);

        TimeEntry lastTimeEntry = new TimeEntry();
        lastTimeEntry.setEntryType(TimeEntryType.CLOCK_IN);

        when(jobAssignmentRepository.findById(any(JobAssignmentId.class)))
            .thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(job.getId()))
            .thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
            .thenReturn(Optional.of(lastTimeEntry));
        when(timeEntryRepository.save(any(TimeEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TimeEntry result = timeEntryService.create(request, employee);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEntryType()).isEqualTo(TimeEntryType.START_BREAK);
        assertThat(result.getEmployee()).isEqualTo(employee);

        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    @Test
    @DisplayName("Should allow END_BREAK when last entry is START_BREAK")
    void create_WhenLastEntryIsStartBreakAndNextIsEndBreak_ShouldSucceed(){

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setJobId(job.getId());
        request.setEntryType(TimeEntryType.END_BREAK);
        request.setLatitude(0.0);
        request.setLongitude(0.0);
        request.setManual(false);

        TimeEntry lastTimeEntry = new TimeEntry();
        lastTimeEntry.setEntryType(TimeEntryType.START_BREAK);

        when(jobAssignmentRepository.findById(any(JobAssignmentId.class)))
            .thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(job.getId()))
            .thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
            .thenReturn(Optional.of(lastTimeEntry));
        when(timeEntryRepository.save(any(TimeEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TimeEntry result = timeEntryService.create(request, employee);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEntryType()).isEqualTo(TimeEntryType.END_BREAK);
        assertThat(result.getEmployee()).isEqualTo(employee);

        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    @Test
    @DisplayName("Should allow CLOCK_OUT when last entry is END_BREAK")
    void create_WhenLastEntryIsEndBreakAndNextIsClockOut_ShouldSucceed(){

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setJobId(job.getId());
        request.setEntryType(TimeEntryType.CLOCK_OUT);
        request.setLatitude(0.0);
        request.setLongitude(0.0);
        request.setManual(false);

        TimeEntry lastTimeEntry = new TimeEntry();
        lastTimeEntry.setEntryType(TimeEntryType.END_BREAK);

        when(jobAssignmentRepository.findById(any(JobAssignmentId.class)))
            .thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(job.getId()))
            .thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
            .thenReturn(Optional.of(lastTimeEntry));
        when(timeEntryRepository.save(any(TimeEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TimeEntry result = timeEntryService.create(request, employee);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEntryType()).isEqualTo(TimeEntryType.CLOCK_OUT);
        assertThat(result.getEmployee()).isEqualTo(employee);

        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    @Test
    @DisplayName("Should allow CLOCK_OUT when last entry is CLOCK_IN")
    void crate_WhenLastEntryIsClockInAndNextIsClockOut_ShouldSucceed(){

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setJobId(job.getId());
        request.setEntryType(TimeEntryType.CLOCK_OUT);
        request.setLatitude(0.0);
        request.setLongitude(0.0);
        request.setManual(false);

        TimeEntry lastTimeEntry = new TimeEntry();
        lastTimeEntry.setEntryType(TimeEntryType.CLOCK_IN);

        when(jobAssignmentRepository.findById(any(JobAssignmentId.class)))
            .thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(job.getId()))
            .thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
            .thenReturn(Optional.of(lastTimeEntry));
        when(timeEntryRepository.save(any(TimeEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TimeEntry result = timeEntryService.create(request, employee);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEntryType()).isEqualTo(TimeEntryType.CLOCK_OUT);
        assertThat(result.getEmployee()).isEqualTo(employee);

        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    @ParameterizedTest
    @EnumSource(value =  TimeEntryType.class, mode = EnumSource.Mode.EXCLUDE, names = "CLOCK_IN")
    @DisplayName("Should throw exception when no previous and next is not CLOCK_IN.")
    void create_WithNoPreviousEntriesAndNextIsNotClockIn_shouldThrowException(TimeEntryType timeEntryType){

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setJobId(job.getId());
        request.setEntryType(timeEntryType);
        request.setLatitude(0.0);
        request.setLongitude(0.0);
        request.setManual(false);

        when(jobAssignmentRepository.findById(any(JobAssignmentId.class)))
            .thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(job.getId()))
            .thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
            .thenReturn(Optional.empty());

        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> timeEntryService.create(request, employee),
            "Was Expected that the create method throws an exception, but it didn't."
        );

        assertThat(thrown.getMessage()).contains("A primeira batida de ponto do dia deve ser 'CLOCK_IN'.");
    }

    @Test
    @DisplayName("Should throw exception when last entry is CLOCK_IN and next is also CLOCK_IN")
    void create_WhenLastEntryIsClockInAndNextIsAlsoClockIn_ShouldThrowException(){

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setJobId(job.getId());
        request.setEntryType(TimeEntryType.CLOCK_IN);
        request.setLatitude(0.0);
        request.setLongitude(0.0);
        request.setManual(false);

        TimeEntry lastTimeEntry = new TimeEntry();
        lastTimeEntry.setEntryType(TimeEntryType.CLOCK_IN);

        when(jobAssignmentRepository.findById(any(JobAssignmentId.class)))
            .thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(job.getId()))
            .thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
            .thenReturn(Optional.of(lastTimeEntry));

        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> timeEntryService.create(request, employee),
            "Was Expected that the create method throws an exception, but it didn't."
        );

        assertThat(thrown.getMessage()).contains("Não é possível fazer 'CLOCK_IN' sem antes ter feito 'CLOCK_OUT'");
    }

    @ParameterizedTest
    @EnumSource(value = TimeEntryType.class)
    @DisplayName("Should throw exception when the last entry is same the next one")
    void create_WhenLastEntryAndNextIsTheSame(TimeEntryType timeEntryType){

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setJobId(job.getId());
        request.setEntryType(timeEntryType);
        request.setLatitude(0.0);
        request.setLongitude(0.0);
        request.setManual(false);

        TimeEntry lastTimeEntry = new TimeEntry();
        lastTimeEntry.setEntryType(timeEntryType);

        Set<String> errorMessages = Set.of(
            "Não é possível fazer 'CLOCK_IN' sem antes ter feito 'CLOCK_OUT'",
            "Só é possível iniciar um intervalo se você estiver trabalhando.",
            "Não é possível terminar um intervalo sem antes tê-lo iniciado.",
            "Não é possível fazer 'CLOCK_OUT' neste momento."
        );

        when(jobAssignmentRepository.findById(any(JobAssignmentId.class)))
            .thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(job.getId()))
            .thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
            .thenReturn(Optional.of(lastTimeEntry));

        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> timeEntryService.create(request, employee),
            "Was Expected that the create method throws an exception, but it didn't."
        );

        assertTrue( errorMessages.contains(thrown.getMessage()), "Unexpected exception message: " + thrown.getMessage());
    }


    @Test
    @DisplayName("Should throw exception when last entry is not START_BREAK and next is END_BREAK")
    void create_WhenLastEntryIsNotStartBreakAndNextIsEndBreak_ShouldThrowException(){

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setJobId(job.getId());
        request.setEntryType(TimeEntryType.END_BREAK);
        request.setLatitude(0.0);
        request.setLongitude(0.0);
        request.setManual(false);

        TimeEntry lastTimeEntry = new TimeEntry();
        lastTimeEntry.setEntryType(TimeEntryType.CLOCK_IN);

        when(jobAssignmentRepository.findById(any(JobAssignmentId.class)))
            .thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(job.getId()))
            .thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
            .thenReturn(Optional.of(lastTimeEntry));

        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> timeEntryService.create(request, employee),
            "Was Expected that the create method throws an exception, but it didn't."
        );

        assertThat(thrown.getMessage()).contains("Não é possível terminar um intervalo sem antes tê-lo iniciado.");
    }

    @Test
    @DisplayName("Should throw exception when last entry is START_BREAK and next is CLOCK_OUT")
    void create_WhenLastEntryIsStartBreakAndNextIsClockOut_ShouldThrowException(){

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setJobId(job.getId());
        request.setEntryType(TimeEntryType.CLOCK_OUT);
        request.setLatitude(0.0);
        request.setLongitude(0.0);
        request.setManual(false);

        TimeEntry lastTimeEntry = new TimeEntry();
        lastTimeEntry.setEntryType(TimeEntryType.START_BREAK);

        when(jobAssignmentRepository.findById(any(JobAssignmentId.class)))
            .thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(job.getId()))
            .thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
            .thenReturn(Optional.of(lastTimeEntry));

        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> timeEntryService.create(request, employee),
            "Was Expected that the create method throws an exception, but it didn't."
        );

        assertThat(thrown.getMessage()).contains("Não é possível fazer 'CLOCK_OUT' neste momento.");
    }

    // Tests about manual Time Entry
    @Test
    @DisplayName("Should allow set a manual time entry when there is no register before")
    void create_WithNoPreviousAndNextIsManualTimeEntry_ShouldSucceed(){

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setJobId(job.getId());
        request.setEntryType(TimeEntryType.CLOCK_IN);
        request.setLatitude(0.0);
        request.setLongitude(0.0);
        request.setManual(true);
        request.setReportedTimestamp(Instant.parse("2025-09-06T08:56:00Z"));
        request.setJustification("Test manual time entry");

        when(jobAssignmentRepository.findById(any(JobAssignmentId.class)))
            .thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(job.getId()))
            .thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
            .thenReturn(Optional.empty());
        when(timeEntryRepository.save(any(TimeEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TimeEntry result = timeEntryService.create(request, employee);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEntryType()).isEqualTo(TimeEntryType.CLOCK_IN);
        assertThat(result.getEmployee()).isEqualTo(employee);
        assertThat(result.getEntryTimestamp()).isEqualTo(request.getReportedTimestamp());
        assertThat(result.getJustification()).isNotNull();

        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"  ", ""})
    @NullSource
    @DisplayName("Should throw exception when is manual and justification is null or blank")
    void create_WhenManualAndJustificationIsNullOrBlank_ShouldThrowException(String justification) {

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setManual(true);
        request.setReportedTimestamp(Instant.now());
        request.setJustification(justification);
        request.setJobId(job.getId());

        when(jobAssignmentRepository.findById(any())).thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(any())).thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId())).thenReturn(Optional.empty());


        // Act & Assert
        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> timeEntryService.create(request, employee)
        );

        assertThat(thrown.getMessage()).isEqualTo("É preciso informar o motivo da batida manual.");
    }

    @Test
    @DisplayName("Should throw exception when manual and reported timestamp is null")
    void create_WhenManualAndReportedTimestampIsNull_ShouldThrowException() {

        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());

        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        var request = new TimeEntryRequestDto();
        request.setManual(true);
        request.setReportedTimestamp(null); // Timestamp anterior
        request.setJustification("Teste");
        request.setJobId(job.getId());

        when(jobAssignmentRepository.findById(any())).thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(any())).thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> timeEntryService.create(request, employee)
        );

        assertThat(thrown.getMessage()).isEqualTo("É preciso informar uma data e hora.");
    }

    @Test
    @DisplayName("Should throw exception when manual and reported timestamp is not after last entry")
    void create_WhenManualAndReportedTimestampIsNotAfterLastEntry_ShouldThrowException() {
        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());
        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        Instant lastTimestamp = Instant.now();
        TimeEntry lastEntry = new TimeEntry();
        lastEntry.setEntryTimestamp(lastTimestamp);


        var request = new TimeEntryRequestDto();
        request.setManual(true);
        request.setReportedTimestamp(lastTimestamp.minusSeconds(60)); // Timestamp anterior
        request.setJustification("Teste");
        request.setJobId(job.getId());

        when(jobAssignmentRepository.findById(any())).thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(any())).thenReturn(Optional.of(job));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId())).thenReturn(Optional.of(lastEntry));

        // Act & Assert
        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> timeEntryService.create(request, employee)
        );
        assertThat(thrown.getMessage()).isEqualTo("A batida de ponto manual não pode ser anterior ou igual à última batida registrada.");
    }

    @Test
    @DisplayName("Should throw exception when employee is not assigned to job")
    void create_WhenEmployeeIsNotAssignedToJob_ShouldThrowException() {
        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());
        var request = new TimeEntryRequestDto();
        request.setJobId(UUID.randomUUID());

        when(jobAssignmentRepository.findById(any(JobAssignmentId.class))).thenReturn(Optional.empty());

        // Act & Assert
        SecurityException thrown = assertThrows(
            SecurityException.class,
            () -> timeEntryService.create(request, employee)
        );
        assertThat(thrown.getMessage()).contains("Você não está designado para este trabalho.");
    }

    @Test
    @DisplayName("Should throw exception when Job status is not IN_PROGRESS")
    void create_WhenJobStatusIsNotInProgress_ShouldThrowException() {
        // Arrange
        var employee = new User();
        employee.setId(UUID.randomUUID());
        var job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.COMPLETED);

        var request = new TimeEntryRequestDto();
        request.setJobId(job.getId());

        when(jobAssignmentRepository.findById(any())).thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        // Act & Assert
        IllegalStateException thrown = assertThrows(
            IllegalStateException.class,
            () -> timeEntryService.create(request, employee)
        );
        assertThat(thrown.getMessage()).isEqualTo("Só é possível bater o ponto em trabalhos com status 'IN_PROGRESS'.");
    }

    @Test
    @DisplayName("Should return a list of entries")
    void findAllByEmployeeAndPeriod_ShouldReturnEntries() {
        // Arrange
        var employeeId = UUID.randomUUID();
        var employee = new User();
        employee.setId(employeeId);

        Instant startDate = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant endDate = Instant.now();

        List<TimeEntry> mockEntries = List.of(new TimeEntry(), new TimeEntry());

        when(timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(employeeId, startDate, endDate))
            .thenReturn(mockEntries);

        // Act
        List<TimeEntry> result = timeEntryService.findAllByEmployeeAndPeriod(employee, startDate, endDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).isEqualTo(mockEntries);
    }

    @Test
    @DisplayName("Should throw exception when start date is after end date")
    void findAllByEmployeeAndPeriod_WhenStartDateIsAfterEndDate_ShouldThrowException() {
        // Arrange
        var employee = new User();

        Instant startDate = Instant.now();
        Instant endDate = startDate.minus(1, ChronoUnit.DAYS);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> timeEntryService.findAllByEmployeeAndPeriod(employee, startDate, endDate),
            "Era esperado que o método lançasse uma IllegalArgumentException, mas não o fez."
        );

        assertThat(thrown.getMessage()).isEqualTo("A data de início não pode ser posterior à data final.");
    }

}
