package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.SalaryHistory;
import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.repository.SalaryHistoryRepository;
import br.com.effies.laboris.backend.domain.repository.TimeEntryRepository;
import br.com.effies.laboris.backend.presentation.dto.response.MyPayrollResponseDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    private final TimeEntryRepository timeEntryRepository;
    private final SalaryHistoryRepository salaryRepository;

    public PayrollService(TimeEntryRepository timeEntryRepository, SalaryHistoryRepository salaryRepository){
        this.salaryRepository = salaryRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    public MyPayrollResponseDto calculateMyPayroll(User employee, Instant start, Instant end){

        List<TimeEntry> entries = timeEntryRepository.findByEmployee_IdAndEntryTimestampBetweenOrderByEntryTimestampAsc(employee.getId(), start, end);

        Map<LocalDate, List<TimeEntry>> entriesByDay = entries.stream().collect(Collectors.groupingBy(entry ->
            entry.getEntryTimestamp().atZone(ZoneOffset.UTC).toLocalDate()));

        List<MyPayrollResponseDto.DailyEntryDto> dailyBreakdown = new ArrayList<>();

        BigDecimal totalOpenAmount = BigDecimal.ZERO;
        BigDecimal totalPaidAmount = BigDecimal.ZERO;
        BigDecimal totalOpensHours = BigDecimal.ZERO;
        BigDecimal totalPaidHours = BigDecimal.ZERO;

        for(Map.Entry<LocalDate, List<TimeEntry>> dayEntry : entriesByDay.entrySet()){
            LocalDate workDate = dayEntry.getKey();
            List<TimeEntry> dayEntries = dayEntry.getValue();

            BigDecimal hoursWorkedOnDay = calculateHoursWorked(dayEntries);

            Optional<SalaryHistory> salary = salaryRepository
                .findTopByUser_IdAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(employee.getId(), workDate);

            if(salary.isPresent()){
                BigDecimal hourlyRate = salary.get().getHourlyRate();
                BigDecimal amountEarnedOnDay =  hourlyRate.multiply(hoursWorkedOnDay).setScale(2, RoundingMode.HALF_UP);

                TimeEntry firstEntry = dayEntries.getFirst();
                boolean isPaid = firstEntry.getPayrollId() != null;
                String status =  isPaid ? "PAID" : "OPEN";

                dailyBreakdown.add(
                    MyPayrollResponseDto.DailyEntryDto.builder()
                        .date(workDate)
                        .jobAddress(firstEntry.getJob().getAddress())
                        .hoursWorked(hoursWorkedOnDay)
                        .amountEarned(amountEarnedOnDay)
                        .status(status)
                        .build());

                if(isPaid){
                    totalPaidHours = totalPaidHours.add(hoursWorkedOnDay);
                    totalPaidAmount = totalPaidAmount.add(amountEarnedOnDay);
                }else{
                    totalOpensHours = totalOpensHours.add(hoursWorkedOnDay);
                    totalOpenAmount = totalOpenAmount.add(amountEarnedOnDay);
                }
            }
        }

        return MyPayrollResponseDto.builder()
            .openToReceive(periodSummaryBuilder(totalOpenAmount, totalOpensHours))
            .alreadyPaid(periodSummaryBuilder(totalPaidAmount, totalPaidHours))
            .dailyBreakdown(dailyBreakdown)
            .build();

    }

    private BigDecimal calculateHoursWorked(List<TimeEntry> entries){

        long totalSeconds = 0;
        Instant startWork = null;
        Instant startBreak = null;

        for( TimeEntry entry : entries) {
            switch (entry.getEntryType()){
                case CLOCK_IN -> { startWork = entry.getEntryTimestamp(); }
                case START_BREAK -> {
                    if( startWork != null ){
                        totalSeconds += Duration.between(startWork, entry.getEntryTimestamp()).getSeconds();
                        startWork = null;
                    }
                    startBreak = entry.getEntryTimestamp();
                }
                case END_BREAK -> {
                    if( startBreak != null ){
                        startWork = entry.getEntryTimestamp();
                        startBreak = null;
                    }
                }
                case CLOCK_OUT -> {
                    if ( startWork != null ){
                        totalSeconds += Duration.between(startWork, entry.getEntryTimestamp()).getSeconds();
                        startWork = null;
                    }
                }
            }
        }

        return convertToHours(totalSeconds);
    }

    private BigDecimal convertToHours(long seconds){
        return BigDecimal.valueOf(seconds / 3600.0).setScale(2, RoundingMode.HALF_UP);
    }

    private MyPayrollResponseDto.PeriodSummary periodSummaryBuilder(BigDecimal amount, BigDecimal hours){
        return MyPayrollResponseDto.PeriodSummary.builder()
            .totalAmount(amount)
            .totalHours(hours)
            .build();
    }
}
