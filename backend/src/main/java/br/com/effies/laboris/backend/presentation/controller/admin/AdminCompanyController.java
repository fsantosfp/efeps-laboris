package br.com.effies.laboris.backend.presentation.controller.admin;

import br.com.effies.laboris.backend.domain.entity.Company;
import br.com.effies.laboris.backend.domain.service.CompanyService;
import br.com.effies.laboris.backend.presentation.dto.request.CreateCompanyRequestDto;
import br.com.effies.laboris.backend.presentation.dto.request.UpdateCompanyStatusDto;
import br.com.effies.laboris.backend.presentation.dto.response.CompanyResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// TODO: Criar o DTO CreateCompanyRequestDto
// TODO: Criar o CompanyService com a lógica de criação
// TODO: Injetar o serviço e chamar o método

@RestController
@RequestMapping("api/v1/admin/companies")
public class AdminCompanyController {

    private final CompanyService companyService;

    public AdminCompanyController(CompanyService companyService){
        this.companyService = companyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SAAS_OWNER')")
    public ResponseEntity<CompanyResponseDto> createCompany(@RequestBody CreateCompanyRequestDto request){

        Company newCompany = companyService.create(request);

        CompanyResponseDto response = new CompanyResponseDto(newCompany);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('SAAS_OWNER')")
    public ResponseEntity<List<CompanyResponseDto>> listCompanies(){
        List<Company> companies = companyService.findAll();
        List<CompanyResponseDto> response = companies.stream().map(CompanyResponseDto::new).toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{companyId}")
    @PreAuthorize("hasRole('SAAS_OWNER')")
    public ResponseEntity<CompanyResponseDto> updateCompanyStatus(
        @PathVariable UUID companyId,
        @Valid @RequestBody UpdateCompanyStatusDto request
    ){
        Company updateCompany = companyService.updateStatus(companyId, request.getStatus());
        CompanyResponseDto response = new CompanyResponseDto(updateCompany);
        return ResponseEntity.ok(response);
    }

}
