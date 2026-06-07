package br.com.effies.laboris.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(min = 8, message = "A nova senha deve ter no mínimo 8 caracteres.")
    private String newPassword;
}
