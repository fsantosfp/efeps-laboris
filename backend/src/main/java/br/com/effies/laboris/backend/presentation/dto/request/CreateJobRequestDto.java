package br.com.effies.laboris.backend.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateJobRequestDto {

    @NotBlank(message = "O endereço é obrigatório.")
    private String address;

    @NotNull(message = "A latitude é obrigatória.")
    private Double latitude;

    @NotNull(message = "A longitude é obrigatória.")
    private Double longitude;

    @NotBlank(message = "O nome do contratante é obrigatório.")
    private String clientName;

    @NotNull(message = "O orçamento é obrigatório.")
    @Positive(message = "O orçamento deve ser positivo.")
    private BigDecimal budget;

    @NotNull(message = "O valor/hora (billing rate) é obrigatório.")
    @Positive(message = "O valor/hora deve ser positivo.")
    private BigDecimal billingRate;

    @NotNull(message = "Data de início é obrigatória.")
    @FutureOrPresent(message = "A data de início não pode ser no passado.")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotBlank(message = "O nome do responsável é obrigatório.")
    private String responsibleName;

    @NotBlank(message = "O telefone do responsável é obrigatório.")
    private String responsiblePhone;

    @Email(message = "E-mail do responsável inválido.")
    private String responsibleEmail;
}
