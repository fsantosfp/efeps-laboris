package br.com.effies.laboris.backend.presentation.dto.response;

import br.com.effies.laboris.backend.domain.entity.User;
import lombok.Data;

import java.util.UUID;

@Data
public class EmployeeSummaryDto {

    private UUID id;
    private String name;

    public EmployeeSummaryDto(User user){
        this.id = user.getId();
        this.name = user.getName();
    }
}
