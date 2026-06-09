package br.com.effies.laboris.backend.presentation.dto.request;

import br.com.effies.laboris.backend.domain.entity.enums.JobStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateJobRequestDto {

    @NotNull(message = "O Status não pode ser nulo.")
    private JobStatus status;

    @NotNull(message = "O valor/hora (billing rate) é obrigatório.")
    @Positive(message = "O valor/hora deve ser positivo.")
    private BigDecimal billingRate;

    @NotNull(message = "O orçamento é obrigatório.")
    @Positive(message = "O orçamento deve ser positivo.")
    private BigDecimal budget;

    @NotNull(message = "Data de início é obrigatória.")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotBlank(message = "O nome do responsável é obrigatório.")
    private String responsibleName;

    @NotBlank(message = "O telefone do responsável é obrigatório.")
    private String responsiblePhone;

    @Email(message = "E-mail do responsável inválido.")
    private String responsibleEmail;
}
