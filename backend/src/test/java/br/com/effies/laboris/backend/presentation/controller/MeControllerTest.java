package br.com.effies.laboris.backend.presentation.controller;

import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.domain.service.TokenService;
import br.com.effies.laboris.backend.presentation.dto.request.ChangePasswordRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeController.class)
@AutoConfigureMockMvc(addFilters = false)
class MeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should successfully change password, set reset flag to false and return 204 No Content")
    void changePassword_WithValidRequest_ShouldUpdatePasswordAndReturnNoContent() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@company.com");
        mockUser.setPasswordResetRequired(true);
        mockUser.setRole(br.com.effies.laboris.backend.domain.entity.enums.UserRole.EMPLOYEE);

        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setNewPassword("newSecurePassword123");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("newSecurePassword123")).thenReturn("hashed-new-password");

        // Act & Assert
        mockMvc.perform(put("/api/v1/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userRepository, times(1)).findById(userId);
        verify(passwordEncoder, times(1)).encode("newSecurePassword123");
        verify(userRepository, times(1)).save(mockUser);

        assertThat(mockUser.getPasswordHash()).isEqualTo("hashed-new-password");
        assertThat(mockUser.isPasswordResetRequired()).isFalse();
    }

    @Test
    @DisplayName("Should return 400 Bad Request when new password is too short")
    void changePassword_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@company.com");
        mockUser.setPasswordResetRequired(true);
        mockUser.setRole(br.com.effies.laboris.backend.domain.entity.enums.UserRole.EMPLOYEE);

        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setNewPassword("short");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act & Assert
        mockMvc.perform(put("/api/v1/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }
}
