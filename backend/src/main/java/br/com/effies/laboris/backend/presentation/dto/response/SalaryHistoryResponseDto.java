package br.com.effies.laboris.backend.presentation.dto.response;

import br.com.effies.laboris.backend.domain.entity.SalaryHistory;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SalaryHistoryResponseDto {

    private UUID id;
    private BigDecimal hourlyRate;
    private LocalDate effectiveDate;
    private Instant createdAt;

    public SalaryHistoryResponseDto(SalaryHistory salaryHistory) {
        this.id = salaryHistory.getId();
        this.hourlyRate = salaryHistory.getHourlyRate();
        this.effectiveDate = salaryHistory.getEffectiveDate();
        this.createdAt = salaryHistory.getCreateAt();
    }
}
