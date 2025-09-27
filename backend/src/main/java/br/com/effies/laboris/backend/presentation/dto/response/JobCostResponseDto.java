package br.com.effies.laboris.backend.presentation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class JobCostResponseDto {

    private JobInfo jobInfo;
    private PeriodSummary periodTotals;
    private List<DailyBreakdownDto> dailyBreakdown;

    @Data
    @Builder
    public static class JobInfo{
        private UUID jobId;
        private String address;
        private String clientName;
        private BigDecimal billingRate;
    }

    @Data
    @Builder
    public static class PeriodSummary{
        private BigDecimal totalHours;
        private BigDecimal totalAmount;
    }

    @Data
    @Builder
    public static class DailyBreakdownDto {
        private LocalDate date;
        private int employeesCount;
        private BigDecimal hoursWorked;
        private BigDecimal amountToBill;
        private LocalTime start;
        private LocalTime end;
    }
}
