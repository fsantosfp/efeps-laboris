package br.com.effies.laboris.backend.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateEmployeeRequestDto {

    @NotBlank(message = "O nome do funcionário é obrigatório.")
    private String name;

    @NotBlank(message = "O e-mail do funcionário é obrigatório.")
    @Email(message = "Formato do e-mail inválido.")
    private String email;

    @NotNull(message = "O valor da hora é obrigatório.")
    @Positive(message = "O valor da hora deve ser positivo.")
    private BigDecimal hourlyRate;

    @NotNull(message = "A data de vigência do salário é obrigatória.")
    private LocalDate effectiveDate;
}
