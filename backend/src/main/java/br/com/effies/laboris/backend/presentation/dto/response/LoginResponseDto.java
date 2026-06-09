package br.com.effies.laboris.backend.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private String token;
    private boolean passwordResetRequired;
}
