package br.com.effies.laboris.backend.presentation.controller;

import br.com.effies.laboris.backend.domain.service.AuthService;
import br.com.effies.laboris.backend.domain.service.TokenService;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.presentation.dto.request.LoginRequestDto;
import br.com.effies.laboris.backend.presentation.dto.response.LoginResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should return passwordResetRequired=false when logging in with standard password")
    void login_WithStandardPassword_ShouldReturnFalseInResponse() throws Exception {
        // Arrange
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("user@company.com");
        request.setPassword("password123");
        LoginResponseDto expectedResponse = new LoginResponseDto("mock-token", false);

        when(authService.login(any(LoginRequestDto.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"))
                .andExpect(jsonPath("$.passwordResetRequired").value(false));
    }

    @Test
    @DisplayName("Should return passwordResetRequired=true when logging in with temporary password")
    void login_WithTemporaryPassword_ShouldReturnTrueInResponse() throws Exception {
        // Arrange
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("newuser@company.com");
        request.setPassword("tempPass12");
        LoginResponseDto expectedResponse = new LoginResponseDto("mock-temp-token", true);

        when(authService.login(any(LoginRequestDto.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-temp-token"))
                .andExpect(jsonPath("$.passwordResetRequired").value(true));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when credentials are invalid")
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        // Arrange
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("user@company.com");
        request.setPassword("wrongpassword");

        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
