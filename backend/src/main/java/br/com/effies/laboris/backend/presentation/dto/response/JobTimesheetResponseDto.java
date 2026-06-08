package br.com.effies.laboris.backend.presentation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class JobTimesheetResponseDto {
    private UUID jobId;
    private String address;
    private List<EmployeeTimesheetDto> employeeTimesheets;

    @Data
    @Builder
    public static class EmployeeTimesheetDto {
        private UUID employeeId;
        private String employeeName;
        private List<DailyHoursDto> dailyHours;
    }

    @Data
    @Builder
    public static class DailyHoursDto {
        private LocalDate date;
        private LocalTime start;
        private LocalTime end;
        private BigDecimal hoursWorked;
        private String displacement;
        private BigDecimal interval;
        private BigDecimal displacementHours;
    }
}
