package br.com.effies.laboris.backend.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class DisplacementRequestDto {

    @NotNull(message = "A latitude é obrigatória.")
    private Double latitude;

    @NotNull(message = "A longitude é obrigatória.")
    private Double longitude;

    private UUID destinationJobId;
}
