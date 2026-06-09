package br.com.effies.laboris.backend.presentation.dto.request;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}
