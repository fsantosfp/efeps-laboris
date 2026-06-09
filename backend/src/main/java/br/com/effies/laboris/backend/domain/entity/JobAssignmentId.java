package br.com.effies.laboris.backend.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Embeddable
public class JobAssignmentId implements Serializable {
    private UUID userId;
    private UUID jobId;
}
