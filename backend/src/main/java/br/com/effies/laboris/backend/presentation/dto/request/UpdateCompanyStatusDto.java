package br.com.effies.laboris.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateCompanyStatusDto {

    @NotBlank(message = "O status não pode ser vazio.")
    @Pattern(regexp = "Active|Inactive", message = "O status deve ser 'Active' ou 'Inactive'.")
    private String status;
}
