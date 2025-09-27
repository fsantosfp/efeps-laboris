package br.com.effies.laboris.backend.presentation.dto.request;

import br.com.effies.laboris.backend.domain.entity.enums.JobStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateJobRequestDto {

    @NotNull(message = "O Status não pode ser nulo.")
    private JobStatus status;

    @Positive
    private BigDecimal billingRate;
    // Futuramente, outros campos para atualização poderiam ser adicionados aqui
    // private String clientName;
    // private BigDecimal budget;
}
