package br.com.effies.laboris.backend.domain.entity;

import br.com.effies.laboris.backend.domain.entity.enums.TimeEntryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "time_entries")
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entry_timestamp", nullable = false)
    private Instant entryTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private TimeEntryType entryType;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "is_manual", nullable = false)
    private boolean manual = false;

    private String justification;

    @Column(name = "payroll_id")
    private UUID payrollId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
}
