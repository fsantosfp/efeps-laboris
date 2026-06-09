package br.com.effies.laboris.backend.presentation.dto.response;

import br.com.effies.laboris.backend.domain.entity.Job;
import lombok.Data;

import java.util.UUID;

@Data
public class MyAssignmentsResponseDto {

    private UUID jobId;
    private Double latitude;
    private Double longitude;
    private String address;

    public MyAssignmentsResponseDto(Job job){
        this.jobId = job.getId();
        this.latitude = job.getLatitude();
        this.longitude = job.getLongitude();
        this.address = job.getAddress();
    }
}
