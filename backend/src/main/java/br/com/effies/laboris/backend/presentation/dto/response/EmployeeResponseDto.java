package br.com.effies.laboris.backend.presentation.dto.response;

import br.com.effies.laboris.backend.domain.entity.SalaryHistory;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.model.Employee;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class EmployeeResponseDto {

    private UUID id;
    private String name;
    private String email;
    private String role;
    private String status;
    private BigDecimal hourlyRate;
    private LocalDate effectiveDate;
    private Instant createdAt;

    public EmployeeResponseDto(Employee employee){

        User user = employee.user();
        SalaryHistory salary = employee.salaryHistory();

        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.status = user.getStatus().name();
        this.hourlyRate = salary != null ? salary.getHourlyRate() : null;
        this.effectiveDate = salary != null ? salary.getEffectiveDate() : null;
        this.createdAt = user.getCreatedAt();

    }
}
