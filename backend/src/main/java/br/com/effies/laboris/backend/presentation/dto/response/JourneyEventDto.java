package br.com.effies.laboris.backend.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class JourneyEventDto {
    private String type; // "WORK", "DISPLACEMENT", "BREAK"
    private LocalDate date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant startTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant endTimestamp;

    private String jobAddress; // Destination address for displacements, Job address for work/breaks
    private String originAddress; // Only populated for displacements
    private BigDecimal durationHours;
}
