package br.com.effies.laboris.backend.presentation.dto.request;

import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TimeEntryRequestDto {

    @NotNull(message = "O ID do trabalho é obrigatório.")
    private UUID jobId;

    @NotNull(message = "O tipo de batida é obrigatório.")
    private TimeEntryType entryType;

    @NotNull(message = "A latitude é obrigatória.")
    private Double latitude;

    @NotNull(message = "A longitude é obrigatória.")
    private Double longitude;

    @JsonProperty("isManual")
    private boolean manual;

    private String justification;

    private Instant reportedTimestamp;
}
