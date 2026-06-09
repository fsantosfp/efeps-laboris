package br.com.effies.laboris.backend.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateSalaryHistoryRequestDto {

    @NotNull(message = "O valor da taxa horária é obrigatório.")
    @Positive(message = "O valor da taxa horária deve ser positivo.")
    private BigDecimal hourlyRate;

    @NotNull(message = "A data de vigência do salário é obrigatória.")
    private LocalDate effectiveDate;
}
