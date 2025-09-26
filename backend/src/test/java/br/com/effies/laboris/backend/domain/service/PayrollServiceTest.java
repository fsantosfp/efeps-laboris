package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.SalaryHistory;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import br.com.effies.laboris.backend.domain.repository.SalaryHistoryRepository;
import br.com.effies.laboris.backend.domain.repository.TimeEntryRepository;
import br.com.effies.laboris.backend.presentation.dto.response.MyPayrollResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PayrollServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private SalaryHistoryRepository salaryRepository;

    @InjectMocks
    private PayrollService payrollService;

    private User employee;
    private Job job;

    @BeforeEach
    void setUp(){
        employee = new User();
        employee.setId(UUID.randomUUID());
        job = new Job();
        job.setAddress("Rua Teste, 123");
    }

    @Test
    @DisplayName("Should calculate correctly my payroll for a day work with a break.")
    void calculateMyPayroll_WhenSingleDayWithBreak_ShouldCalculateCorrectly(){
        // Arrange
        LocalDate workDate = LocalDate.now();
        Instant start = workDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = start.plus(1, ChronoUnit.DAYS);

        List<TimeEntry> entries = List.of(
            createTimeEntry(TimeEntryType.CLOCK_IN, start.plus(9, ChronoUnit.HOURS)),
            createTimeEntry(TimeEntryType.START_BREAK, start.plus(12, ChronoUnit.HOURS)),
            createTimeEntry(TimeEntryType.END_BREAK, start.plus(13, ChronoUnit.HOURS)),
            createTimeEntry(TimeEntryType.CLOCK_OUT, start.plus(18, ChronoUnit.HOURS))
        );

        SalaryHistory salary = createSalaryHistory(new BigDecimal("25.00"), workDate.minusDays(1));

        when(timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(employee.getId(), start, end))
            .thenReturn(entries);

        when(salaryRepository.findTopByUser_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(employee.getId(), workDate))
            .thenReturn(Optional.of(salary));

        // Act
        MyPayrollResponseDto result = payrollService.calculateMyPayroll(employee, start, end);

        // Assert
        assertThat(result.getOpenToReceive().getTotalHours()).isEqualTo(8.0);
        assertThat(result.getOpenToReceive().getTotalAmount()).isEqualByComparingTo("200.00");
        assertThat(result.getAlreadyPaid().getTotalHours()).isEqualTo(0.0);
        assertThat(result.getAlreadyPaid().getTotalAmount()).isEqualByComparingTo("0");
        assertThat(result.getDailyBreakdown()).hasSize(1);
        assertThat(result.getDailyBreakdown().getFirst().getStatus()).isEqualTo("OPEN");
    }

    @Test
    @DisplayName("Should use correct rate each day when salary changes during the period.")
    void calculateMyPayroll_WhenSalaryChangesDuringPeriod_ShouldUseCorrectRateEachDay(){
        // Arrange
        LocalDate day1 = LocalDate.now();
        LocalDate day2 = day1.plusDays(1);
        Instant start = day1.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = day2.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<TimeEntry> entries = List.of(
            createTimeEntry(TimeEntryType.CLOCK_IN, start.plus(9, ChronoUnit.HOURS)),   // Day 1 - 09:00
            createTimeEntry(TimeEntryType.CLOCK_OUT, start.plus(17, ChronoUnit.HOURS)), // Day 1 - 17:00
            createTimeEntry(TimeEntryType.CLOCK_IN, start.plus(33, ChronoUnit.HOURS)), // Day 2 - 09:00
            createTimeEntry(TimeEntryType.CLOCK_OUT, start.plus(41, ChronoUnit.HOURS))  // Day 2 - 17:00
        );

        SalaryHistory oldSalary = createSalaryHistory(new BigDecimal("20.00"), day1.minusDays(10));
        SalaryHistory newSalary = createSalaryHistory( new BigDecimal("30.00"), day2);

        when(timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(employee.getId(), start, end))
            .thenReturn(entries);
        when(salaryRepository.findTopByUser_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(employee.getId(), day1))
            .thenReturn(Optional.of(oldSalary));
        when(salaryRepository.findTopByUser_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(employee.getId(), day2))
            .thenReturn(Optional.of(newSalary));

        // Act
        MyPayrollResponseDto result = payrollService.calculateMyPayroll(employee, start, end);

        // Assert
        assertThat(result.getDailyBreakdown()).hasSize(2);
        assertThat(result.getAlreadyPaid().getTotalAmount()).isEqualByComparingTo("0");
        assertThat(result.getOpenToReceive().getTotalHours()).isEqualTo(16.0);

        // Dia 1 (8h * 20.00 = 160.00) + Dia 2 (8h * 30.00 = 240.00) = 400.00
        assertThat(result.getOpenToReceive().getTotalAmount()).isEqualByComparingTo("400.00");

    }

    @Test
    @DisplayName("Should separate totals by paid and open entries")
    void calculateMyPayroll_WhitPaidAndOpenEntries_ShouldSeparateTotal(){
        // Arrange
        LocalDate day1 = LocalDate.now();
        LocalDate day2 = day1.plusDays(1);
        Instant start = day1.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = day2.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        UUID payrollId = UUID.randomUUID();

        List<TimeEntry> entries = List.of(
            createTimeEntry(TimeEntryType.CLOCK_IN, start.plus(9, ChronoUnit.HOURS), payrollId), // Day 1
            createTimeEntry(TimeEntryType.CLOCK_OUT, start.plus(14, ChronoUnit.HOURS), payrollId), // Day 1 -> 5h
            createTimeEntry(TimeEntryType.CLOCK_IN, start.plus(33, ChronoUnit.HOURS), null), // Day 2
            createTimeEntry(TimeEntryType.CLOCK_OUT, start.plus(38, ChronoUnit.HOURS), null)  // Day 2 -> 5h
        );

        SalaryHistory salary = createSalaryHistory(new BigDecimal("10.00"), day1.minusDays(1));

        when(timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(any(), any(), any()))
            .thenReturn(entries);
        when(salaryRepository.findTopByUser_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(any(), any()))
            .thenReturn(Optional.of(salary));

        // Act
        MyPayrollResponseDto result = payrollService.calculateMyPayroll(employee, start, end);

        // Assert
        assertThat(result.getAlreadyPaid().getTotalHours()).isEqualTo(5.0);
        assertThat(result.getAlreadyPaid().getTotalAmount()).isEqualByComparingTo("50.00");
        assertThat(result.getOpenToReceive().getTotalHours()).isEqualTo(5.0);
        assertThat(result.getOpenToReceive().getTotalAmount()).isEqualByComparingTo("50.00");
        assertThat(result.getDailyBreakdown()
            .stream().filter(d -> d.getStatus().equals("PAID")).count()).isEqualTo(1);
        assertThat(result.getDailyBreakdown()
            .stream().filter(d -> d.getStatus().equals("OPEN")).count()).isEqualTo(1);

    }

    @Test
    @DisplayName("Should return zeroed totals when no entries")
    void calculateMyPayroll_WhenNoEntries_ShouldReturnZeroedTotals(){
        // Arrange
        Instant start = Instant.now();
        Instant end = start.plus(1, ChronoUnit.DAYS);
        when(timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(any(), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act
        MyPayrollResponseDto result = payrollService.calculateMyPayroll(employee, start, end);

        // Assert
        assertThat(result.getOpenToReceive().getTotalAmount()).isEqualByComparingTo("0");
        assertThat(result.getAlreadyPaid().getTotalAmount()).isEqualByComparingTo("0");
        assertThat(result.getDailyBreakdown()).isEmpty();
    }

    @Test
    @DisplayName("Should return zeroed totals when has only clock in entry")
    void calculateMyPayroll_WhenHasOnlyClockIn_shouldReturnZeroedTotals(){
        // Arrange
        LocalDate workDate = LocalDate.now();
        Instant start = workDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = start.plus(1, ChronoUnit.DAYS);

        List<TimeEntry> entries = List.of(
            createTimeEntry(TimeEntryType.CLOCK_IN, start.plus(9, ChronoUnit.HOURS))
        );

        SalaryHistory salary = createSalaryHistory(new BigDecimal("25.00"), workDate.minusDays(1));

        when(timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(employee.getId(), start, end))
            .thenReturn(entries);

        when(salaryRepository.findTopByUser_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(employee.getId(), workDate))
            .thenReturn(Optional.of(salary));

        // Act
        MyPayrollResponseDto result = payrollService.calculateMyPayroll(employee, start, end);

        // Assert
        assertThat(result.getOpenToReceive().getTotalAmount()).isEqualByComparingTo("0");
        assertThat(result.getAlreadyPaid().getTotalAmount()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Should calculate only worded hours when last entry is end break")
    void calculateMyPayroll_WhenLastEntryIsEndBreak_shouldCalculateOnlyWorkedHours(){
        // Arrange
        LocalDate workDate = LocalDate.now();
        Instant start = workDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = start.plus(1, ChronoUnit.DAYS);

        List<TimeEntry> entries = List.of(
            createTimeEntry(TimeEntryType.CLOCK_IN, start.plus(9, ChronoUnit.HOURS)),
            createTimeEntry(TimeEntryType.START_BREAK, start.plus(12, ChronoUnit.HOURS)),
            createTimeEntry(TimeEntryType.END_BREAK, start.plus(13, ChronoUnit.HOURS))
        );

        SalaryHistory salary = createSalaryHistory(new BigDecimal("10.00"), workDate.minusDays(1));

        when(timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(employee.getId(), start, end))
            .thenReturn(entries);

        when(salaryRepository.findTopByUser_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(employee.getId(), workDate))
            .thenReturn(Optional.of(salary));

        // Act
        MyPayrollResponseDto result = payrollService.calculateMyPayroll(employee, start, end);

        // Assert
        assertThat(result.getOpenToReceive().getTotalAmount()).isEqualByComparingTo("30.00");
        assertThat(result.getAlreadyPaid().getTotalAmount()).isEqualByComparingTo("0");
        assertThat(result.getDailyBreakdown()).hasSize(1);
    }

    private TimeEntry createTimeEntry(TimeEntryType type, Instant timestamp){
        return createTimeEntry(type, timestamp, null);
    }

    private TimeEntry createTimeEntry(TimeEntryType type, Instant timestamp, UUID payrollId){
        TimeEntry entry = new TimeEntry();
        entry.setEmployee(this.employee);
        entry.setJob(this.job);
        entry.setEntryType(type);
        entry.setEntryTimestamp(timestamp);
        entry.setPayrollId(payrollId);
        return entry;
    }

    private SalaryHistory createSalaryHistory(BigDecimal rate, LocalDate effectiveDate){
        SalaryHistory salary = new SalaryHistory();
        salary.setHourlyRate(rate);
        salary.setEffectiveDate(effectiveDate);
        salary.setUser(this.employee);
        return salary;
    }

}


