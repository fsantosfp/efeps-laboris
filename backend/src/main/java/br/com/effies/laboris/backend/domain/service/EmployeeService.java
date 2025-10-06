package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.SalaryHistory;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.UserRole;
import br.com.effies.laboris.backend.domain.entity.enums.UserStatus;
import br.com.effies.laboris.backend.domain.model.Employee;
import br.com.effies.laboris.backend.domain.repository.SalaryHistoryRepository;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.presentation.dto.request.CreateEmployeeRequestDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final SalaryHistoryRepository salaryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
        SalaryHistoryRepository salaryRepository,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.salaryRepository = salaryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Employee create(CreateEmployeeRequestDto request, User manager) {

        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
        });

        User employee = new User();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setRole(UserRole.EMPLOYEE);
        employee.setCompany(manager.getCompany());

        String randomPassword = UUID.randomUUID().toString();
        employee.setPasswordHash(passwordEncoder.encode(randomPassword));

        User savedEmployee = userRepository.save(employee);

        SalaryHistory initialSalary = new SalaryHistory();
        initialSalary.setUser(savedEmployee);
        initialSalary.setHourlyRate(request.getHourlyRate());
        initialSalary.setEffectiveDate(request.getEffectiveDate());
        salaryRepository.save(initialSalary);

        // TODO: Disparar e-mail de boas-vindas para o funcionário
        System.out.println("LOG DEV: E-mail de boas-vindas para o funcionário " + employee.getEmail());

        return new Employee(savedEmployee, initialSalary);
    }

    public List<Employee> findAllByManager(User manager) {

        UUID companyId = manager.getCompany().getId();

        List<User> employees = userRepository.findByCompanyIdAndRoleAndStatus(companyId, UserRole.EMPLOYEE, UserStatus.ACTIVE);

        return employees.stream()
            .map(employee -> {
                SalaryHistory currentSalary = salaryRepository
                    .findTopByUser_IdOrderByEffectiveDateDesc(employee.getId())
                    .orElse(null);

                return new Employee(employee, currentSalary);
            }).collect(Collectors.toList());
    }

    @Transactional
    public void deactivate(UUID employeeId, User manager){

        User employee = userRepository.findById(employeeId)
            .orElseThrow(() -> new EntityNotFoundException("Funcionario não encontrado"));

        if(!employee.getCompany().getId().equals(manager.getCompany().getId())){
            throw new SecurityException("Acesso negado. Você não pode modificar funcionários de outra empresa.");
        }

        employee.setStatus(UserStatus.INACTIVE);
        userRepository.save(employee);
    }

}