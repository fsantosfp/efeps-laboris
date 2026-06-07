package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Company;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.UserRole;
import br.com.effies.laboris.backend.domain.repository.CompanyRepository;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.presentation.dto.request.CreateCompanyRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CompanyService companyService;

    @Test
    @DisplayName("Should successfully create company and manager, and send welcome email")
    void createCompany_ShouldSaveAndSendEmail() {
        // Arrange
        CreateCompanyRequestDto request = new CreateCompanyRequestDto();
        request.setCompanyName("Test Company");
        request.setManagerName("Manager Name");
        request.setManagerEmail("manager@company.com");

        Company savedCompany = new Company();
        savedCompany.setName(request.getCompanyName());

        when(userRepository.findByEmail(request.getManagerEmail())).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("hashed_password");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Company result = companyService.create(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Company");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo("Manager Name");
        assertThat(savedUser.getEmail()).isEqualTo("manager@company.com");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.MANAGER);
        assertThat(savedUser.isPasswordResetRequired()).isTrue();

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendWelcomeEmail(
            eq("manager@company.com"),
            eq("Manager Name"),
            passwordCaptor.capture(),
            eq("MANAGER")
        );
        assertThat(passwordCaptor.getValue()).hasSize(10);
    }

    @Test
    @DisplayName("Should throw exception when manager email already exists")
    void createCompany_WhenEmailExists_ShouldThrowException() {
        // Arrange
        CreateCompanyRequestDto request = new CreateCompanyRequestDto();
        request.setManagerEmail("exists@company.com");

        when(userRepository.findByEmail(request.getManagerEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThatThrownBy(() -> companyService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Já existe um cadastrado com este e-mail");

        verifyNoInteractions(companyRepository);
        verifyNoInteractions(emailService);
    }
}
