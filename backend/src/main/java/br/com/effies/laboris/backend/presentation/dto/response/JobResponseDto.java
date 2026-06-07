package br.com.effies.laboris.backend.presentation.dto.response;

import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.User;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class JobResponseDto {

    private UUID id;
    private String address;
    private Double latitude;
    private Double longitude;
    private String clientName;
    private String status;
    private BigDecimal billingRate;
    private BigDecimal budget;
    private LocalDate startDate;
    private LocalDate endDate;
    private String responsibleName;
    private String responsiblePhone;
    private String responsibleEmail;
    private Instant createdAt;
    private List<EmployeeSummaryDto> assignedTeam;

    public JobResponseDto(Job job){
        this.id = job.getId();
        this.address = job.getAddress();
        this.latitude = job.getLatitude();
        this.longitude = job.getLongitude();
        this.clientName = job.getClientName();
        this.status = job.getStatus().name();
        this.billingRate = job.getBillingRate();
        this.budget = job.getBudget();
        this.startDate = job.getStartDate();
        this.endDate = job.getEndDate();
        this.responsibleName = job.getResponsibleName();
        this.responsiblePhone = job.getResponsiblePhone();
        this.responsibleEmail = job.getResponsibleEmail();
        this.createdAt = job.getCreatedAt();
    }

    public JobResponseDto(Job job, List<User> assignedEmployees){
        this(job);
        this.assignedTeam = assignedEmployees.stream()
            .map(EmployeeSummaryDto::new).toList();
    }
}
