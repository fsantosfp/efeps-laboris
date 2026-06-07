package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Company;
import br.com.effies.laboris.backend.domain.entity.SalaryHistory;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.UserRole;
import br.com.effies.laboris.backend.domain.model.Employee;
import br.com.effies.laboris.backend.domain.repository.SalaryHistoryRepository;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.presentation.dto.request.CreateEmployeeRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private SalaryHistoryRepository salaryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    @DisplayName("Should successfully create employee and initial salary, and send welcome email")
    void createEmployee_ShouldSaveAndSendEmail() {
        // Arrange
        Company company = new Company();
        company.setId(UUID.randomUUID());

        User manager = new User();
        manager.setCompany(company);

        CreateEmployeeRequestDto request = new CreateEmployeeRequestDto();
        request.setName("John Doe");
        request.setEmail("john.doe@company.com");
        request.setHourlyRate(new BigDecimal("25.50"));
        request.setEffectiveDate(LocalDate.now());

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("hashed_temp_password");
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        when(salaryRepository.save(any(SalaryHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Employee result = employeeService.create(request, manager);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.user().getName()).isEqualTo("John Doe");
        assertThat(result.user().isPasswordResetRequired()).isTrue();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(UserRole.EMPLOYEE);
        assertThat(savedUser.isPasswordResetRequired()).isTrue();

        verify(salaryRepository).save(any(SalaryHistory.class));

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendWelcomeEmail(
            eq("john.doe@company.com"),
            eq("John Doe"),
            passwordCaptor.capture(),
            eq("EMPLOYEE")
        );
        assertThat(passwordCaptor.getValue()).hasSize(10);
    }

    @Test
    @DisplayName("Should throw exception when employee email already exists")
    void createEmployee_WhenEmailExists_ShouldThrowException() {
        // Arrange
        User manager = new User();
        CreateEmployeeRequestDto request = new CreateEmployeeRequestDto();
        request.setEmail("john.doe@company.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThatThrownBy(() -> employeeService.create(request, manager))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Já existe um usuário cadastrado com este e-mail.");

        verifyNoInteractions(salaryRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Should successfully retrieve salary history for employee belonging to same company")
    void findSalariesByEmployee_WithSameCompany_ShouldReturnList() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);

        User manager = new User();
        manager.setCompany(company);

        UUID employeeId = UUID.randomUUID();
        User employee = new User();
        employee.setId(employeeId);
        employee.setCompany(company);
        employee.setRole(UserRole.EMPLOYEE);

        java.util.List<SalaryHistory> salaries = java.util.List.of(new SalaryHistory());

        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(salaryRepository.findAllByUser_IdOrderByEffectiveDateDesc(employeeId)).thenReturn(salaries);

        // Act
        java.util.List<SalaryHistory> result = employeeService.findSalariesByEmployee(employeeId, manager);

        // Assert
        assertThat(result).isEqualTo(salaries);
        verify(userRepository, times(1)).findById(employeeId);
        verify(salaryRepository, times(1)).findAllByUser_IdOrderByEffectiveDateDesc(employeeId);
    }

    @Test
    @DisplayName("Should throw SecurityException when manager company does not match employee company")
    void findSalariesByEmployee_WithDifferentCompany_ShouldThrowSecurityException() {
        // Arrange
        Company company1 = new Company();
        company1.setId(UUID.randomUUID());
        User manager = new User();
        manager.setCompany(company1);

        Company company2 = new Company();
        company2.setId(UUID.randomUUID());
        UUID employeeId = UUID.randomUUID();
        User employee = new User();
        employee.setId(employeeId);
        employee.setCompany(company2);

        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThatThrownBy(() -> employeeService.findSalariesByEmployee(employeeId, manager))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Acesso negado");

        verifyNoInteractions(salaryRepository);
    }

    @Test
    @DisplayName("Should successfully add a new salary history entry for valid employee")
    void addSalaryHistory_WithSameCompany_ShouldSaveAndReturn() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);

        User manager = new User();
        manager.setCompany(company);

        UUID employeeId = UUID.randomUUID();
        User employee = new User();
        employee.setId(employeeId);
        employee.setCompany(company);
        employee.setRole(UserRole.EMPLOYEE);

        br.com.effies.laboris.backend.presentation.dto.request.CreateSalaryHistoryRequestDto request =
            new br.com.effies.laboris.backend.presentation.dto.request.CreateSalaryHistoryRequestDto();
        request.setHourlyRate(new BigDecimal("35.00"));
        request.setEffectiveDate(LocalDate.now());

        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(salaryRepository.save(any(SalaryHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SalaryHistory result = employeeService.addSalaryHistory(employeeId, request, manager);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(employee);
        assertThat(result.getHourlyRate()).isEqualTo(new BigDecimal("35.00"));
        verify(salaryRepository, times(1)).save(any(SalaryHistory.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when trying to add salary history to user who is not an employee")
    void addSalaryHistory_WithNonEmployeeRole_ShouldThrowIllegalArgumentException() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);

        User manager = new User();
        manager.setCompany(company);

        UUID employeeId = UUID.randomUUID();
        User employee = new User();
        employee.setId(employeeId);
        employee.setCompany(company);
        employee.setRole(UserRole.MANAGER); // Different role!

        br.com.effies.laboris.backend.presentation.dto.request.CreateSalaryHistoryRequestDto request =
            new br.com.effies.laboris.backend.presentation.dto.request.CreateSalaryHistoryRequestDto();
        request.setHourlyRate(new BigDecimal("35.00"));
        request.setEffectiveDate(LocalDate.now());

        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThatThrownBy(() -> employeeService.addSalaryHistory(employeeId, request, manager))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("não é um funcionário");

        verify(salaryRepository, never()).save(any());
    }
}
