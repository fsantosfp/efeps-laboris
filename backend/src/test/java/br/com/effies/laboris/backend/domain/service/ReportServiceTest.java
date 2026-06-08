package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Company;
import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import br.com.effies.laboris.backend.domain.repository.DisplacementRepository;
import br.com.effies.laboris.backend.domain.repository.JobRepository;
import br.com.effies.laboris.backend.domain.repository.TimeEntryRepository;
import br.com.effies.laboris.backend.domain.utils.TimeEntryBuilder;
import br.com.effies.laboris.backend.presentation.dto.response.JobCostResponseDto;
import br.com.effies.laboris.backend.presentation.dto.response.JobTimesheetResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private TimeEntryRepository timeEntryRepository;
    @Mock
    private DisplacementRepository displacementRepository;
    @InjectMocks
    private ReportService reportService;

    private User manager, employee1, employee2;
    private Job job;
    private Instant start, end;

    @BeforeEach
    void setUp() {
        var company = new Company();
        company.setId(UUID.randomUUID());
        manager = createUser(UUID.randomUUID(), company);
        employee1 = createUser(UUID.randomUUID(), company);
        employee2 = createUser(UUID.randomUUID(), company);

        job = new Job();
        job.setId(UUID.randomUUID());
        job.setCompany(company);
        job.setBillingRate(new BigDecimal("50.00")); // R$ 50/hora

        start = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        end = start.plus(1, ChronoUnit.DAYS);
    }

    @Test
    @DisplayName("Deve agrupar funcionários que fizeram o mesmo turno no relatório de custo")
    void calculateJobCostReport_WhenEmployeesWorkSameShift_ShouldGroupThem() {
        // Arrange
        // Ambos funcionários trabalharam das 08:00 às 17:00 (9h totais, 8h líquidas)
        List<TimeEntry> entries = List.of(
            TimeEntryBuilder.aTimeEntry().withClockIn().withJob(job).atTime(8).forUser(employee1).build(),
            TimeEntryBuilder.aTimeEntry().withClockOut().withJob(job).atTime(12).forUser(employee1).build(),
            TimeEntryBuilder.aTimeEntry().withClockIn().withJob(job).atTime(13).forUser(employee1).build(),
            TimeEntryBuilder.aTimeEntry().withClockOut().withJob(job).atTime(17).forUser(employee1).build(),
            TimeEntryBuilder.aTimeEntry().withClockIn().withJob(job).atTime(8).forUser(employee2).build(),
            TimeEntryBuilder.aTimeEntry().withClockOut().withJob(job).atTime(12).forUser(employee2).build(),
            TimeEntryBuilder.aTimeEntry().withClockIn().withJob(job).atTime(13).forUser(employee2).build(),
            TimeEntryBuilder.aTimeEntry().withClockOut().withJob(job).atTime(17).forUser(employee2).build()
        );

        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(timeEntryRepository.findAllByJobIdAndPeriod(job.getId(), start, end)).thenReturn(entries);

        // Act
        JobCostResponseDto result = reportService.calculateJobCostReport(manager, job.getId(), start, end);

        // Assert
        assertThat(result.getDailyBreakdown()).hasSize(1);
        JobCostResponseDto.DailyBreakdownDto group = result.getDailyBreakdown().getFirst();
        assertThat(group.getEmployeesCount()).isEqualTo(2);
        assertThat(group.getHoursWorked()).isEqualByComparingTo("8.00"); // 8 horas líquidas do func 1
        // (8h * R$50/h * 2 pessoas) = R$ 800.00
        assertThat(group.getAmountToBill()).isEqualByComparingTo("800.00");
    }

    @Test
    @DisplayName("Deve criar grupos diferentes para funcionários com turnos diferentes no mesmo dia")
    void calculateJobCostReport_WhenEmployeesWorkDifferentShifts_ShouldCreateSeparateGroups() {
        // Arrange
        List<TimeEntry> entries = List.of(
            // Func 1: 08:00 - 12:00 (4h)
            TimeEntryBuilder.aTimeEntry().forUser(employee1).withJob(job).withClockIn().atTime(8).build(),
            TimeEntryBuilder.aTimeEntry().forUser(employee1).withJob(job).withClockOut().atTime(12).build(),

            TimeEntryBuilder.aTimeEntry().forUser(employee2).withJob(job).withClockIn().atTime(9).build(),
            TimeEntryBuilder.aTimeEntry().forUser(employee2).withJob(job).withClockOut().atTime(12).build()
        );
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(timeEntryRepository.findAllByJobIdAndPeriod(job.getId(), start, end)).thenReturn(entries);

        // Act
        JobCostResponseDto result = reportService.calculateJobCostReport(manager, job.getId(), start, end);

        // Assert
        assertThat(result.getDailyBreakdown()).hasSize(2);
        // Total: (4h * R$50) + (3h * R$50) = 200 + 150 = 350
        assertThat(result.getPeriodTotals().getTotalAmount()).isEqualByComparingTo("350.00");
    }

    @Test
    @DisplayName("Deve retornar um relatório vazio se não houver batidas de ponto")
    void calculateJobCostReport_WhenNoTimeEntries_ShouldReturnEmptyReport() {
        // Arrange
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(timeEntryRepository.findAllByJobIdAndPeriod(job.getId(), start, end)).thenReturn(Collections.emptyList());

        // Act
        JobCostResponseDto result = reportService.calculateJobCostReport(manager, job.getId(), start, end);

        // Assert
        assertThat(result.getDailyBreakdown()).isEmpty();
        assertThat(result.getPeriodTotals().getTotalAmount()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Deve calcular corretamente o relatório de custo mesmo se a taxa de faturamento (billingRate) do Job for nula")
    void calculateJobCostReport_WhenBillingRateIsNull_ShouldNotThrowNpe() {
        // Arrange
        job.setBillingRate(null); // Null billing rate!
        List<TimeEntry> entries = List.of(
            TimeEntryBuilder.aTimeEntry().withClockIn().withJob(job).atTime(8).forUser(employee1).build(),
            TimeEntryBuilder.aTimeEntry().withClockOut().withJob(job).atTime(12).forUser(employee1).build()
        );

        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(timeEntryRepository.findAllByJobIdAndPeriod(job.getId(), start, end)).thenReturn(entries);

        // Act
        JobCostResponseDto result = reportService.calculateJobCostReport(manager, job.getId(), start, end);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDailyBreakdown()).hasSize(1);
        assertThat(result.getPeriodTotals().getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getPeriodTotals().getTotalHours()).isEqualByComparingTo("4.00");
    }

    @Test
    @DisplayName("Deve somar as horas de deslocamento no relatório de custo de trabalho")
    void calculateJobCostReport_WhenDisplacementExists_ShouldIncludeDisplacementHours() {
        // Arrange
        // Func 1 trabalhou das 08:00 às 12:00 (4h líquidas) no job
        List<TimeEntry> entries = List.of(
            TimeEntryBuilder.aTimeEntry().withClockIn().withJob(job).atTime(8).forUser(employee1).build(),
            TimeEntryBuilder.aTimeEntry().withClockOut().withJob(job).atTime(12).forUser(employee1).build()
        );

        // Deslocamento de 1.5 horas finalizado no mesmo dia
        var displacement = new br.com.effies.laboris.backend.domain.entity.Displacement();
        displacement.setUser(employee1);
        displacement.setDestinationJob(job);
        displacement.setStartTimestamp(entries.get(0).getEntryTimestamp().minus(90, ChronoUnit.MINUTES));
        displacement.setEndTimestamp(entries.get(0).getEntryTimestamp());

        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(timeEntryRepository.findAllByJobIdAndPeriod(job.getId(), start, end)).thenReturn(entries);
        when(displacementRepository.findAllByDestinationJobIdAndPeriod(job.getId(), start, end)).thenReturn(List.of(displacement));

        // Act
        JobCostResponseDto result = reportService.calculateJobCostReport(manager, job.getId(), start, end);

        // Assert
        assertThat(result.getDailyBreakdown()).hasSize(1);
        JobCostResponseDto.DailyBreakdownDto group = result.getDailyBreakdown().getFirst();
        // 4h trabalhadas + 1.5h de deslocamento = 5.5h
        assertThat(group.getHoursWorked()).isEqualByComparingTo("5.50");
        // (5.5h * R$50/h * 1 pessoa) = R$ 275.00
        assertThat(group.getAmountToBill()).isEqualByComparingTo("275.00");
    }

    @Test
    @DisplayName("Deve calcular corretamente as horas por colaborador dentro do período fornecido")
    void calculateJobTimesheetReport_WhenPeriodProvided_ShouldFilterAndGroupCorrectly() {
        // Arrange
        employee1.setName("Alice");
        employee2.setName("Bob");

        List<TimeEntry> entries = List.of(
            TimeEntryBuilder.aTimeEntry().withClockIn().withJob(job).atTime(8).forUser(employee1).build(),
            TimeEntryBuilder.aTimeEntry().withClockOut().withJob(job).atTime(12).forUser(employee1).build(),
            TimeEntryBuilder.aTimeEntry().withClockIn().withJob(job).atTime(9).forUser(employee2).build(),
            TimeEntryBuilder.aTimeEntry().withClockOut().withJob(job).atTime(15).forUser(employee2).build()
        );

        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(timeEntryRepository.findAllByJobIdAndPeriod(job.getId(), start, end)).thenReturn(entries);
        when(timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(any(UUID.class), any(Instant.class), any(Instant.class))).thenReturn(Collections.emptyList());
        when(displacementRepository.findAllByUserIdAndDestinationJobIdAndPeriod(any(UUID.class), any(UUID.class), any(Instant.class), any(Instant.class))).thenReturn(Collections.emptyList());

        // Act
        JobTimesheetResponseDto result = reportService.calculateJobTimesheetReport(manager, job.getId(), start, end);

        // Assert
        assertThat(result.getJobId()).isEqualTo(job.getId());
        assertThat(result.getEmployeeTimesheets()).hasSize(2);

        // Sorted by employee name: Alice, then Bob
        var aliceReport = result.getEmployeeTimesheets().get(0);
        assertThat(aliceReport.getEmployeeName()).isEqualTo("Alice");
        assertThat(aliceReport.getDailyHours()).hasSize(1);
        assertThat(aliceReport.getDailyHours().get(0).getHoursWorked()).isEqualByComparingTo("4.00");
        assertThat(aliceReport.getDailyHours().get(0).getStart()).isEqualTo(java.time.LocalTime.of(8, 0));
        assertThat(aliceReport.getDailyHours().get(0).getEnd()).isEqualTo(java.time.LocalTime.of(12, 0));

        var bobReport = result.getEmployeeTimesheets().get(1);
        assertThat(bobReport.getEmployeeName()).isEqualTo("Bob");
        assertThat(bobReport.getDailyHours()).hasSize(1);
        assertThat(bobReport.getDailyHours().get(0).getHoursWorked()).isEqualByComparingTo("6.00");
        assertThat(bobReport.getDailyHours().get(0).getStart()).isEqualTo(java.time.LocalTime.of(9, 0));
        assertThat(bobReport.getDailyHours().get(0).getEnd()).isEqualTo(java.time.LocalTime.of(15, 0));
    }

    @Test
    @DisplayName("Deve buscar todas as batidas e calcular horas por colaborador se o período não for fornecido")
    void calculateJobTimesheetReport_WhenPeriodNotProvided_ShouldFetchAllAndGroupCorrectly() {
        // Arrange
        employee1.setName("Alice");

        List<TimeEntry> entries = List.of(
            TimeEntryBuilder.aTimeEntry().withClockIn().withJob(job).atTime(8).forUser(employee1).build(),
            TimeEntryBuilder.aTimeEntry().withClockOut().withJob(job).atTime(12).forUser(employee1).build()
        );

        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(timeEntryRepository.findAllByJobIdOrderByEntryTimestampAsc(job.getId())).thenReturn(entries);
        when(timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(any(UUID.class), any(Instant.class), any(Instant.class))).thenReturn(Collections.emptyList());
        when(displacementRepository.findAllByUserIdAndDestinationJobIdAndPeriod(any(UUID.class), any(UUID.class), any(Instant.class), any(Instant.class))).thenReturn(Collections.emptyList());

        // Act
        JobTimesheetResponseDto result = reportService.calculateJobTimesheetReport(manager, job.getId(), null, null);

        // Assert
        assertThat(result.getJobId()).isEqualTo(job.getId());
        assertThat(result.getEmployeeTimesheets()).hasSize(1);
        var aliceReport = result.getEmployeeTimesheets().getFirst();
        assertThat(aliceReport.getEmployeeName()).isEqualTo("Alice");
        assertThat(aliceReport.getDailyHours().getFirst().getHoursWorked()).isEqualByComparingTo("4.00");
    }

    @Test
    @DisplayName("Deve retornar um timesheet vazio se não houver batidas no período")
    void calculateJobTimesheetReport_WhenNoEntries_ShouldReturnEmptyTimesheet() {
        // Arrange
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(timeEntryRepository.findAllByJobIdAndPeriod(job.getId(), start, end)).thenReturn(Collections.emptyList());

        // Act
        JobTimesheetResponseDto result = reportService.calculateJobTimesheetReport(manager, job.getId(), start, end);

        // Assert
        assertThat(result.getEmployeeTimesheets()).isEmpty();
    }

    // Métodos auxiliares
    private User createUser(UUID id, Company company) {
        User user = new User();
        user.setId(id);
        user.setCompany(company);
        return  user;
    }
}
