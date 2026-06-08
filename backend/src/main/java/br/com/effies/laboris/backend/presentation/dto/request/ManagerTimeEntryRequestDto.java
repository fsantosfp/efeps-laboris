package br.com.effies.laboris.backend.presentation.dto.request;

import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ManagerTimeEntryRequestDto {

    @NotNull(message = "O ID do trabalho é obrigatório.")
    private UUID jobId;

    @NotNull(message = "O tipo de batida é obrigatório.")
    private TimeEntryType entryType;

    @NotNull(message = "A data e hora são obrigatórias.")
    private Instant timestamp;

    @NotBlank(message = "A justificativa é obrigatória.")
    private String justification;
}
