package br.com.effies.laboris.backend.presentation.dto.response;

import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TimeEntryResponseDto {

    private UUID id;
    private UUID jobId;
    private String entryType;
    private Instant timestamp;
    @JsonProperty("isManual")
    private boolean isManual;
    private String justification;

    public TimeEntryResponseDto(TimeEntry timeEntry){
        this.id = timeEntry.getId();
        this.jobId = timeEntry.getJob().getId();
        this.entryType = timeEntry.getEntryType().name();
        this.timestamp = timeEntry.getEntryTimestamp();
        this.isManual = timeEntry.isManual();
        this.justification = timeEntry.getJustification();
    }
}
