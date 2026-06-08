package br.com.effies.laboris.backend.presentation.dto.response;

import br.com.effies.laboris.backend.domain.entity.Displacement;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class DisplacementResponseDto {

    private UUID id;
    private UUID userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant startTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant endTimestamp;

    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private String startAddress;
    private UUID destinationJobId;

    public DisplacementResponseDto(Displacement displacement) {
        if (displacement == null) {
            return;
        }
        this.id = displacement.getId();
        this.userId = displacement.getUser() != null ? displacement.getUser().getId() : null;
        this.startTimestamp = displacement.getStartTimestamp();
        this.endTimestamp = displacement.getEndTimestamp();
        this.startLatitude = displacement.getStartLatitude();
        this.startLongitude = displacement.getStartLongitude();
        this.endLatitude = displacement.getEndLatitude();
        this.endLongitude = displacement.getEndLongitude();
        this.startAddress = displacement.getStartAddress();
        this.destinationJobId = displacement.getDestinationJob() != null ? displacement.getDestinationJob().getId() : null;
    }
}
