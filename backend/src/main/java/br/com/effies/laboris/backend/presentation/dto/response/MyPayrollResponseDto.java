package br.com.effies.laboris.backend.presentation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MyPayrollResponseDto {

    private PeriodSummary openToReceive;
    private PeriodSummary alreadyPaid;
    private List<DailyEntryDto> dailyBreakdown;

    @Data
    @Builder
    public static class PeriodSummary{
        private BigDecimal totalHours;
        private BigDecimal totalAmount;
    }

    @Data
    @Builder
    public static class DailyEntryDto{
        private LocalDate date;
        private String jobAddress;
        private BigDecimal hoursWorked;
        private BigDecimal amountEarned;
        private String status;
    }
}
