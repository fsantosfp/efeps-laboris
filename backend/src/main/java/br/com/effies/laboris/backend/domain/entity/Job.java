package br.com.effies.laboris.backend.domain.entity;

import br.com.effies.laboris.backend.domain.entity.enums.JobStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "client_name")
    private String clientName;

    @Column(nullable = false)
    private BigDecimal budget;

    @Column(name = "billing_rate", nullable = false)
    private BigDecimal billingRate;

    @Column(name = "responsible_name", nullable = false)
    private String responsibleName;

    @Column(name = "responsible_phone", nullable = false)
    private String responsiblePhone;

    @Column(name = "responsible_email")
    private String responsibleEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public void ensureBelongsTo(User user){
        if (!this.getCompany().getId().equals(user.getCompany().getId())){
            throw new SecurityException("Acesso negado.");
        }
    }

    public void ensureNotExcluded(){
        if (this.getStatus().equals(JobStatus.DELETED)){
         throw new IllegalStateException("Não é possivel modificar um trabalho deletado.");
        }
    }
}
