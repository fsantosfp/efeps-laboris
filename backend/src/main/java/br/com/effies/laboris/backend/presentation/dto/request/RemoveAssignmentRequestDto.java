package br.com.effies.laboris.backend.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RemoveAssignmentRequestDto {
    @NotEmpty(message = "A lista de funcionários não pode ser vazia.")
    private List<UUID> employeeIds;
}
