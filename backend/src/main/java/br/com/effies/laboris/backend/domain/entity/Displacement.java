package br.com.effies.laboris.backend.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "displacements")
public class Displacement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_timestamp", nullable = false)
    private Instant startTimestamp;

    @Column(name = "end_timestamp")
    private Instant endTimestamp;

    @Column(name = "start_latitude", nullable = false)
    private Double startLatitude;

    @Column(name = "start_longitude", nullable = false)
    private Double startLongitude;

    @Column(name = "end_latitude")
    private Double endLatitude;

    @Column(name = "end_longitude")
    private Double endLongitude;

    @Column(name = "start_address")
    private String startAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_job_id")
    private Job destinationJob;
}
