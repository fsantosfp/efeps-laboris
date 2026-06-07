package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Company;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.UserRole;
import br.com.effies.laboris.backend.domain.repository.CompanyRepository;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.presentation.dto.request.CreateCompanyRequestDto;
import br.com.effies.laboris.backend.domain.utils.PasswordGenerator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public CompanyService(
        CompanyRepository companyRepository, 
        UserRepository userRepository, 
        PasswordEncoder passwordEncoder,
        EmailService emailService
    ){
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public Company create(CreateCompanyRequestDto request){

        userRepository.findByEmail(request.getManagerEmail()).ifPresent(user -> {
            throw new IllegalArgumentException("Já existe um cadastrado com este e-mail");
        });

        Company newCompany = new Company();
        newCompany.setName(request.getCompanyName());
        Company savedCompany = companyRepository.save(newCompany);

        String temporaryPassword = PasswordGenerator.generate();
        var manager = managerCreate(request, savedCompany, temporaryPassword);

        emailService.sendWelcomeEmail(manager.getEmail(), manager.getName(), temporaryPassword, manager.getRole().name());

        return savedCompany;

    }

    public List<Company> findAll(){
        return companyRepository.findAll();
    }

    @Transactional
    public Company updateStatus(UUID companyId, String newStatus){

        Company company = companyRepository.findById(companyId)
            .orElseThrow( () -> new EntityNotFoundException("Empresa não encontrada."));

        company.setStatus(newStatus);
        return companyRepository.save(company);
    }

    private User managerCreate(CreateCompanyRequestDto request, Company company, String temporaryPassword){
        User manager = new User();
        manager.setName(request.getManagerName());
        manager.setEmail(request.getManagerEmail());
        manager.setRole(UserRole.MANAGER);
        manager.setCompany(company);
        manager.setPasswordResetRequired(true);

        manager.setPasswordHash(passwordEncoder.encode(temporaryPassword));

        return userRepository.save(manager);
    }
}
