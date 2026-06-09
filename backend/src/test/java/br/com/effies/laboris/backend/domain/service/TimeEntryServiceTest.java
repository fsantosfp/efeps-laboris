package br.com.effies.laboris.backend.domain.service;


import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.JobAssignment;
import br.com.effies.laboris.backend.domain.entity.JobAssignmentId;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.JobStatus;
import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import br.com.effies.laboris.backend.domain.repository.DisplacementRepository;
import br.com.effies.laboris.backend.domain.repository.JobAssignmentRepository;
import br.com.effies.laboris.backend.domain.repository.JobRepository;
import br.com.effies.laboris.backend.domain.repository.TimeEntryRepository;
import br.com.effies.laboris.backend.presentation.dto.request.TimeEntryRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para TimeEntryService")
class TimeEntryServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobAssignmentRepository jobAssignmentRepository;
    @Mock
    private DisplacementRepository displacementRepository;
    @InjectMocks
    private TimeEntryService timeEntryService;

    // Objetos base que serão recriados antes de cada teste
    private User employee;
    private Job job;
    private TimeEntryRequestDto request;

    @BeforeEach
    void setUp() {
        // ARRANGE Centralizado: Prepara os objetos comuns a todos os testes
        employee = new User();
        employee.setId(UUID.randomUUID());

        job = new Job();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.IN_PROGRESS);

        request = new TimeEntryRequestDto();
        request.setJobId(job.getId());
        request.setLatitude(0.0);
        request.setLongitude(0.0);
        request.setManual(false);
    }

    @Test
    @DisplayName("Deve criar um TimeEntry com sucesso no cenário ideal")
    void create_ShouldSucceed_OnHappyPath() {
        // Arrange
        request.setEntryType(TimeEntryType.IN);
        when(jobAssignmentRepository.findById(any(JobAssignmentId.class))).thenReturn(Optional.of(new JobAssignment()));
        when(jobRepository.findById(any(UUID.class))).thenReturn(Optional.of(job));
        when(displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId())).thenReturn(Optional.empty());
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId())).thenReturn(Optional.empty());
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TimeEntry result = timeEntryService.create(request, employee);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEntryType()).isEqualTo(TimeEntryType.IN);
        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    @Nested
    @DisplayName("Validações de Regras de Negócio e Segurança")
    class BusinessRuleValidationTests {

        @Test
        @DisplayName("Deve lançar exceção se o funcionário não estiver designado para o trabalho")
        void create_WhenEmployeeIsNotAssigned_ShouldThrowSecurityException() {
            // Arrange
            when(jobAssignmentRepository.findById(any(JobAssignmentId.class))).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(SecurityException.class, () -> timeEntryService.create(request, employee));
        }

        @Test
        @DisplayName("Deve lançar exceção se o trabalho não estiver 'IN_PROGRESS'")
        void create_WhenJobIsNotInProgress_ShouldThrowIllegalStateException() {
            // Arrange
            job.setStatus(JobStatus.COMPLETED);
            when(jobAssignmentRepository.findById(any(JobAssignmentId.class))).thenReturn(Optional.of(new JobAssignment()));
            when(jobRepository.findById(any(UUID.class))).thenReturn(Optional.of(job));

            // Act & Assert
            IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> timeEntryService.create(request, employee));
            assertThat(thrown.getMessage()).isEqualTo("Só é possível bater o ponto em trabalhos com status 'IN_PROGRESS'.");
        }

        @Test
        @DisplayName("Deve lançar exceção se houver deslocamento ativo ao bater ponto de entrada")
        void create_WhenDisplacementIsActiveOnClockIn_ShouldThrowIllegalStateException() {
            // Arrange
            request.setEntryType(TimeEntryType.IN);
            when(jobAssignmentRepository.findById(any(JobAssignmentId.class))).thenReturn(Optional.of(new JobAssignment()));
            when(jobRepository.findById(any(UUID.class))).thenReturn(Optional.of(job));
            when(displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId())).thenReturn(Optional.of(new br.com.effies.laboris.backend.domain.entity.Displacement()));

            // Act & Assert
            IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> timeEntryService.create(request, employee));
            assertThat(thrown.getMessage()).contains("Não é possível bater ponto de entrada enquanto houver um deslocamento ativo em andamento");
        }

        @Test
        @DisplayName("Deve lançar exceção se a batida ocorrer menos de 60 segundos após a anterior")
        void create_WhenTimeBetweenPunchesIsLessThan60Seconds_ShouldThrowException() {
            // Arrange
            TimeEntry lastEntry = new TimeEntry();
            lastEntry.setEntryTimestamp(Instant.now().minus(30, ChronoUnit.SECONDS));

            when(jobAssignmentRepository.findById(any(JobAssignmentId.class))).thenReturn(Optional.of(new JobAssignment()));
            when(jobRepository.findById(any(UUID.class))).thenReturn(Optional.of(job));
            when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId())).thenReturn(Optional.of(lastEntry));

            // Act & Assert
            IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> timeEntryService.create(request, employee));
            assertThat(thrown.getMessage()).isEqualTo("A batida de ponto não pode ser igual à última batida registrada.");
        }
    }

    @Nested
    @DisplayName("Validações da Máquina de Estados")
    class StateMachineValidationTests {

        @Test
        @DisplayName("Deve falhar se a primeira batida não for CLOCK_IN")
        void create_WhenFirstEntryIsNotClockIn_ShouldThrowException() {
            // Arrange
            request.setEntryType(TimeEntryType.OUT);
            when(jobAssignmentRepository.findById(any(JobAssignmentId.class))).thenReturn(Optional.of(new JobAssignment()));
            when(jobRepository.findById(any(UUID.class))).thenReturn(Optional.of(job));
            when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId())).thenReturn(Optional.empty());

            // Act & Assert
            IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> timeEntryService.create(request, employee));
            assertThat(thrown.getMessage()).isEqualTo("A primeira batida de ponto deve ser uma entrada (IN).");
        }

        @Test
        @DisplayName("Deve falhar ao tentar fazer IN se já estiver em IN")
        void create_WhenAlreadyClockedIn_ShouldThrowExceptionOnClockIn() {
            // Arrange
            TimeEntry lastEntry = new TimeEntry();
            lastEntry.setEntryType(TimeEntryType.IN);
            lastEntry.setEntryTimestamp(Instant.now().minus(1, ChronoUnit.DAYS));
            when(jobAssignmentRepository.findById(any(JobAssignmentId.class))).thenReturn(Optional.of(new JobAssignment()));
            when(jobRepository.findById(any(UUID.class))).thenReturn(Optional.of(job));
            when(displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId())).thenReturn(Optional.empty());
            when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId())).thenReturn(Optional.of(lastEntry));
            request.setEntryType(TimeEntryType.IN);

            // Act & Assert
            IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> timeEntryService.create(request, employee));
            assertThat(thrown.getMessage()).isEqualTo("Ação inválida. A última batida já foi do tipo 'IN'.");
        }
    }

    @Nested
    @DisplayName("Validações de Batida Manual")
    class ManualEntryValidationTests {

        @Test
        @DisplayName("Deve falhar se for manual e a justificativa for nula ou em branco")
        void create_WhenManualAndJustificationIsBlank_ShouldThrowException() {
            // Arrange
            request.setManual(true);
            request.setReportedTimestamp(Instant.now());
            request.setJustification("   ");

            when(jobAssignmentRepository.findById(any(JobAssignmentId.class))).thenReturn(Optional.of(new JobAssignment()));
            when(jobRepository.findById(any(UUID.class))).thenReturn(Optional.of(job));

            // Act & Assert
            IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> timeEntryService.create(request, employee));
            assertThat(thrown.getMessage()).isEqualTo("É preciso informar o motivo da batida manual.");
        }

        @Test
        @DisplayName("Deve falhar se for manual e o 'reportedTimestamp' for nulo")
        void create_WhenManualAndTimestampIsNull_ShouldThrowException() {
            // Arrange
            request.setManual(true);
            request.setJustification("Teste");
            request.setReportedTimestamp(null);

            when(jobAssignmentRepository.findById(any(JobAssignmentId.class))).thenReturn(Optional.of(new JobAssignment()));
            when(jobRepository.findById(any(UUID.class))).thenReturn(Optional.of(job));

            // Act & Assert
            IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> timeEntryService.create(request, employee));
            assertThat(thrown.getMessage()).isEqualTo("É preciso informar uma data e hora.");
        }

        @Test
        @DisplayName("Deve falhar se for manual e o 'reportedTimestamp' for anterior à última batida")
        void create_WhenManualTimestampIsBeforeLast_ShouldThrowException() {
            // Arrange
            request.setManual(true);
            request.setJustification("Teste");
            request.setReportedTimestamp(Instant.now().minus(1, ChronoUnit.HOURS));

            TimeEntry lastEntry = new TimeEntry();
            lastEntry.setEntryTimestamp(Instant.now());
            when(jobAssignmentRepository.findById(any(JobAssignmentId.class))).thenReturn(Optional.of(new JobAssignment()));
            when(jobRepository.findById(any(UUID.class))).thenReturn(Optional.of(job));
            when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId())).thenReturn(Optional.of(lastEntry));

            // Act & Assert
            IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> timeEntryService.create(request, employee));
            assertThat(thrown.getMessage()).isEqualTo("A batida de ponto não pode ser igual à última batida registrada.");
        }
    }
}