package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.SalaryHistory;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.UserRole;
import br.com.effies.laboris.backend.domain.entity.enums.UserStatus;
import br.com.effies.laboris.backend.domain.model.Employee;
import br.com.effies.laboris.backend.domain.repository.SalaryHistoryRepository;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.domain.utils.PasswordGenerator;
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
    private final EmailService emailService;

    public EmployeeService(
        SalaryHistoryRepository salaryRepository,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        EmailService emailService
    ) {
        this.salaryRepository = salaryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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
        employee.setPasswordResetRequired(true);

        String temporaryPassword = PasswordGenerator.generate();
        employee.setPasswordHash(passwordEncoder.encode(temporaryPassword));

        User savedEmployee = userRepository.save(employee);

        SalaryHistory initialSalary = new SalaryHistory();
        initialSalary.setUser(savedEmployee);
        initialSalary.setHourlyRate(request.getHourlyRate());
        initialSalary.setEffectiveDate(request.getEffectiveDate());
        salaryRepository.save(initialSalary);

        emailService.sendWelcomeEmail(savedEmployee.getEmail(), savedEmployee.getName(), temporaryPassword, savedEmployee.getRole().name());

        return new Employee(savedEmployee, initialSalary);
    }

    public List<Employee> findAllByManager(User manager) {

        UUID companyId = manager.getCompany().getId();

        List<User> employees = userRepository.findByCompanyIdAndRole(companyId, UserRole.EMPLOYEE);

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

    @Transactional
    public void activate(UUID employeeId, User manager){

        User employee = userRepository.findById(employeeId)
            .orElseThrow(() -> new EntityNotFoundException("Funcionario não encontrado"));

        if(!employee.getCompany().getId().equals(manager.getCompany().getId())){
            throw new SecurityException("Acesso negado. Você não pode modificar funcionários de outra empresa.");
        }

        employee.setStatus(UserStatus.ACTIVE);
        userRepository.save(employee);
    }

    public List<SalaryHistory> findSalariesByEmployee(UUID employeeId, User manager) {
        User employee = userRepository.findById(employeeId)
            .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado"));

        if (!employee.getCompany().getId().equals(manager.getCompany().getId())) {
            throw new SecurityException("Acesso negado. Você não pode visualizar salários de funcionários de outra empresa.");
        }

        if (employee.getRole() != UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("O usuário especificado não é um funcionário.");
        }

        return salaryRepository.findAllByUser_IdOrderByEffectiveDateDesc(employeeId);
    }

    @Transactional
    public SalaryHistory addSalaryHistory(UUID employeeId, br.com.effies.laboris.backend.presentation.dto.request.CreateSalaryHistoryRequestDto request, User manager) {
        User employee = userRepository.findById(employeeId)
            .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado"));

        if (!employee.getCompany().getId().equals(manager.getCompany().getId())) {
            throw new SecurityException("Acesso negado. Você não pode modificar salários de funcionários de outra empresa.");
        }

        if (employee.getRole() != UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("O usuário especificado não é um funcionário.");
        }

        SalaryHistory salaryHistory = new SalaryHistory();
        salaryHistory.setUser(employee);
        salaryHistory.setHourlyRate(request.getHourlyRate());
        salaryHistory.setEffectiveDate(request.getEffectiveDate());

        return salaryRepository.save(salaryHistory);
    }
}