package br.com.effies.laboris.backend.presentation.dto.request;

import jakarta.validation.constraints.Future;
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

    @NotBlank(message = "O nome do contratante é obrigatório.")
    private  String clientName;

    @Positive
    private BigDecimal budget;

    private BigDecimal billingRate;

    @NotNull(message = "Data de início é obrigatória.")
    @Future(message = "A data de inicio não pode ser no passado.")
    private LocalDate startDate;

    private LocalDate endDate;
}
