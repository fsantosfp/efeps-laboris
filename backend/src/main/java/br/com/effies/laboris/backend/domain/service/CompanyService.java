package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Company;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.UserRole;
import br.com.effies.laboris.backend.domain.repository.CompanyRepository;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.presentation.dto.request.CreateCompanyRequestDto;
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

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Company create(CreateCompanyRequestDto request){

        userRepository.findByEmail(request.getManagerEmail()).ifPresent(user -> {
            throw new IllegalArgumentException("Já existe um cadastrado com este e-mail");
        });

        Company newCompany = new Company();
        newCompany.setName(request.getCompanyName());
        Company savedCompany = companyRepository.save(newCompany);

        var manager = managerCreate(request, savedCompany);

        // TODO: Implementar a lógica de envio de e-mail aqui.
        // O e-mail conteria um link para o gestor definir sua senha.
        System.out.println("LOG DE DESENVOLVIMENTO: E-mail de boas-vindas deveria ser enviado para " + manager.getEmail());

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

    private User managerCreate(CreateCompanyRequestDto request, Company company){
        User manager = new User();
        manager.setName(request.getManagerName());
        manager.setEmail(request.getManagerEmail());
        manager.setRole(UserRole.MANAGER);
        manager.setCompany(company);

        String randomPassword = UUID.randomUUID().toString();
        manager.setPasswordHash(passwordEncoder.encode(randomPassword));

        return userRepository.save(manager);
    }
}
