package br.com.effies.laboris.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class SettlePayrollRequestDto {

    @NotNull
    private Instant startDate;

    @NotNull
    private Instant endDate;

    @NotEmpty
    private List<UUID> employeeIds;

}


