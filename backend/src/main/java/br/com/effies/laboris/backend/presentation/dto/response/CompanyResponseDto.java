package br.com.effies.laboris.backend.presentation.dto.response;

import br.com.effies.laboris.backend.domain.entity.Company;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CompanyResponseDto {

    private UUID id;

    private String name;

    private String status;

    private Instant createdAt;

    public CompanyResponseDto(Company company){
        this.id = company.getId();
        this.name = company.getName();
        this.status = company.getStatus();
        this.createdAt = company.getCreatedAt();
    }
}
