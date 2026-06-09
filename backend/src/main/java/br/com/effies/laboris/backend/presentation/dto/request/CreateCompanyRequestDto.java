package br.com.effies.laboris.backend.presentation.dto.request;

import lombok.Data;

@Data
public class CreateCompanyRequestDto {
    private String companyName;
    private String managerName;
    private String managerEmail;
}
