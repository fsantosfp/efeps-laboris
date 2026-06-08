package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Displacement;
import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.JobAssignment;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.JobStatus;
import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import br.com.effies.laboris.backend.domain.repository.DisplacementRepository;
import br.com.effies.laboris.backend.domain.repository.JobAssignmentRepository;
import br.com.effies.laboris.backend.domain.repository.JobRepository;
import br.com.effies.laboris.backend.domain.repository.TimeEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para DisplacementService")
class DisplacementServiceTest {

    @Mock
    private DisplacementRepository displacementRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobAssignmentRepository jobAssignmentRepository;
    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private GeoService geoService;

    @InjectMocks
    private DisplacementService displacementService;

    private User employee;
    private Job destinationJob;
    private Displacement activeDisplacement;

    @BeforeEach
    void setUp() {
        employee = new User();
        employee.setId(UUID.randomUUID());

        destinationJob = new Job();
        destinationJob.setId(UUID.randomUUID());
        destinationJob.setLatitude(40.7128);
        destinationJob.setLongitude(-74.0060);
        destinationJob.setStatus(JobStatus.IN_PROGRESS);

        activeDisplacement = new Displacement();
        activeDisplacement.setId(UUID.randomUUID());
        activeDisplacement.setUser(employee);
    }

    @Test
    @DisplayName("Deve iniciar deslocamento com sucesso no cenário ideal")
    void startDisplacement_ShouldSucceed_OnHappyPath() {
        // Arrange
        TimeEntry lastEntry = new TimeEntry();
        lastEntry.setEntryType(TimeEntryType.OUT);

        when(jobAssignmentRepository.findByUserIdAndJobStatus(employee.getId(), JobStatus.IN_PROGRESS))
                .thenReturn(List.of(new JobAssignment(), new JobAssignment()));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
                .thenReturn(Optional.of(lastEntry));
        when(displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId()))
                .thenReturn(Optional.empty());
        when(geoService.reverseGeocode(40.0, -70.0)).thenReturn("Starting Address");
        when(displacementRepository.save(any(Displacement.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Displacement result = displacementService.startDisplacement(40.0, -70.0, employee);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(employee);
        assertThat(result.getStartLatitude()).isEqualTo(40.0);
        assertThat(result.getStartLongitude()).isEqualTo(-70.0);
        assertThat(result.getStartAddress()).isEqualTo("Starting Address");
        verify(displacementRepository).save(any(Displacement.class));
    }

    @Test
    @DisplayName("Deve falhar ao iniciar se usuário tiver menos de 2 jobs ativos")
    void startDisplacement_WhenLessThanTwoJobs_ShouldThrowException() {
        // Arrange
        when(jobAssignmentRepository.findByUserIdAndJobStatus(employee.getId(), JobStatus.IN_PROGRESS))
                .thenReturn(List.of(new JobAssignment()));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> displacementService.startDisplacement(40.0, -70.0, employee));
    }

    @Test
    @DisplayName("Deve falhar ao iniciar se última batida não for OUT")
    void startDisplacement_WhenLastPunchIsNotOut_ShouldThrowException() {
        // Arrange
        TimeEntry lastEntry = new TimeEntry();
        lastEntry.setEntryType(TimeEntryType.IN);

        when(jobAssignmentRepository.findByUserIdAndJobStatus(employee.getId(), JobStatus.IN_PROGRESS))
                .thenReturn(List.of(new JobAssignment(), new JobAssignment()));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
                .thenReturn(Optional.of(lastEntry));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> displacementService.startDisplacement(40.0, -70.0, employee));
    }

    @Test
    @DisplayName("Deve falhar ao iniciar se já houver viagem ativa")
    void startDisplacement_WhenAlreadyActiveDisplacement_ShouldThrowException() {
        // Arrange
        TimeEntry lastEntry = new TimeEntry();
        lastEntry.setEntryType(TimeEntryType.OUT);

        when(jobAssignmentRepository.findByUserIdAndJobStatus(employee.getId(), JobStatus.IN_PROGRESS))
                .thenReturn(List.of(new JobAssignment(), new JobAssignment()));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
                .thenReturn(Optional.of(lastEntry));
        when(displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId()))
                .thenReturn(Optional.of(activeDisplacement));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> displacementService.startDisplacement(40.0, -70.0, employee));
    }

    @Test
    @DisplayName("Deve finalizar deslocamento com sucesso quando próximo ao destino")
    void endDisplacement_ShouldSucceed_WhenCloseToDestination() {
        // Arrange
        Job originJob = new Job();
        originJob.setId(UUID.randomUUID());
        TimeEntry lastEntry = new TimeEntry();
        lastEntry.setJob(originJob);

        when(displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId()))
                .thenReturn(Optional.of(activeDisplacement));
        when(jobRepository.findById(destinationJob.getId())).thenReturn(Optional.of(destinationJob));
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
                .thenReturn(Optional.of(lastEntry));
        when(displacementRepository.save(any(Displacement.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Displacement result = displacementService.endDisplacement(40.72, -74.01, destinationJob.getId(), employee);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEndLatitude()).isEqualTo(40.72);
        assertThat(result.getEndLongitude()).isEqualTo(-74.01);
        assertThat(result.getDestinationJob()).isEqualTo(destinationJob);
        assertThat(result.getEndTimestamp()).isNotNull();
        verify(displacementRepository).save(any(Displacement.class));
    }

    @Test
    @DisplayName("Deve falhar ao finalizar se estiver longe do destino (>= 0.1 graus)")
    void endDisplacement_WhenFarFromDestination_ShouldThrowException() {
        // Arrange
        when(displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId()))
                .thenReturn(Optional.of(activeDisplacement));
        when(jobRepository.findById(destinationJob.getId())).thenReturn(Optional.of(destinationJob));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                displacementService.endDisplacement(41.0, -74.0, destinationJob.getId(), employee));
    }

    @Test
    @DisplayName("Deve falhar ao finalizar se destino for igual a origem")
    void endDisplacement_WhenDestinationEqualsOrigin_ShouldThrowException() {
        // Arrange
        when(displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId()))
                .thenReturn(Optional.of(activeDisplacement));
        when(jobRepository.findById(destinationJob.getId())).thenReturn(Optional.of(destinationJob));

        TimeEntry lastEntry = new TimeEntry();
        lastEntry.setJob(destinationJob); // Same destination job
        when(timeEntryRepository.findTopByEmployee_IdOrderByEntryTimestampDesc(employee.getId()))
                .thenReturn(Optional.of(lastEntry));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                displacementService.endDisplacement(40.72, -74.01, destinationJob.getId(), employee));
    }

    @Test
    @DisplayName("Deve cancelar deslocamento ativo com sucesso")
    void cancelDisplacement_ShouldDeleteActiveDisplacement() {
        // Arrange
        when(displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId()))
                .thenReturn(Optional.of(activeDisplacement));

        // Act
        displacementService.cancelDisplacement(employee);

        // Assert
        verify(displacementRepository).delete(activeDisplacement);
    }

    @Test
    @DisplayName("Deve falhar ao cancelar se não houver deslocamento ativo")
    void cancelDisplacement_WhenNoActiveDisplacement_ShouldThrowException() {
        // Arrange
        when(displacementRepository.findByUser_IdAndEndTimestampIsNull(employee.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> displacementService.cancelDisplacement(employee));
    }

    @Test
    @DisplayName("Deve buscar deslocamentos do usuário por período")
    void getMyDisplacements_ShouldReturnList() {
        // Arrange
        java.time.Instant start = java.time.Instant.now();
        java.time.Instant end = start.plus(1, java.time.temporal.ChronoUnit.DAYS);
        List<Displacement> displacements = List.of(new Displacement());
        when(displacementRepository.findAllByUserIdAndPeriod(employee.getId(), start, end)).thenReturn(displacements);

        // Act
        List<Displacement> result = displacementService.getMyDisplacements(employee, start, end);

        // Assert
        assertThat(result).hasSize(1);
        verify(displacementRepository).findAllByUserIdAndPeriod(employee.getId(), start, end);
    }
}
