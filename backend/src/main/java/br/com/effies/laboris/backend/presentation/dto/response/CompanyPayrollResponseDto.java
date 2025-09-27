package br.com.effies.laboris.backend.presentation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CompanyPayrollResponseDto {

    private PeriodSummary periodTotals;
    private List<EmployeePayrollDto> employeePayrolls;

    @Data
    @Builder
    public static class PeriodSummary{
        private BigDecimal totalAmount;
        private BigDecimal totalHours;
    }

    @Data
    @Builder
    public static  class EmployeePayrollDto{
        private UUID employeeId;
        private String employeeName;
        private BigDecimal totalAmount;
        private BigDecimal totalHours;
        //TODO: Futuramente, poderíamos adicionar o detalhamento diário aqui também
    }

}
