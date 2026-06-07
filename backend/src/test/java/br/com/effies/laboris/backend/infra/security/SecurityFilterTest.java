package br.com.effies.laboris.backend.infra.security;

import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.domain.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SecurityFilterTest {

    private SecurityFilter securityFilter;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        securityFilter = new SecurityFilter(tokenService, userRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        closeable.close();
    }

    @Test
    @DisplayName("Should proceed with filter chain when no authorization header is present")
    void doFilterInternal_WithNoToken_ShouldProceed() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should proceed and authenticate when token is valid and password reset is not required")
    void doFilterInternal_WithValidTokenNoReset_ShouldAuthenticateAndProceed() throws Exception {
        // Arrange
        String token = "valid-token";
        String email = "user@company.com";
        User user = new User();
        user.setEmail(email);
        user.setPasswordResetRequired(false);
        user.setRole(br.com.effies.laboris.backend.domain.entity.enums.UserRole.EMPLOYEE);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenService.validateToken(token)).thenReturn(email);
        when(tokenService.isPasswordResetRequired(token)).thenReturn(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
    }

    @Test
    @DisplayName("Should block with 403 when token is valid, password reset is required, and accessing protected route")
    void doFilterInternal_WithValidTokenResetRequiredAndProtectedRoute_ShouldBlock() throws Exception {
        // Arrange
        String token = "temp-token";
        String email = "newuser@company.com";
        User user = new User();
        user.setEmail(email);
        user.setPasswordResetRequired(true);
        user.setRole(br.com.effies.laboris.backend.domain.entity.enums.UserRole.EMPLOYEE);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/v1/time-entries");
        when(request.getMethod()).thenReturn("GET");
        when(tokenService.validateToken(token)).thenReturn(email);
        when(tokenService.isPasswordResetRequired(token)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, times(1)).setContentType("application/json;charset=UTF-8");
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(stringWriter.toString()).contains("PASSWORD_RESET_REQUIRED");
    }

    @Test
    @DisplayName("Should proceed when token is valid, password reset is required, but accessing PUT /api/v1/me/password")
    void doFilterInternal_WithValidTokenResetRequiredAndChangePasswordRoute_ShouldProceed() throws Exception {
        // Arrange
        String token = "temp-token";
        String email = "newuser@company.com";
        User user = new User();
        user.setEmail(email);
        user.setPasswordResetRequired(true);
        user.setRole(br.com.effies.laboris.backend.domain.entity.enums.UserRole.EMPLOYEE);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/v1/me/password");
        when(request.getMethod()).thenReturn("PUT");
        when(tokenService.validateToken(token)).thenReturn(email);
        when(tokenService.isPasswordResetRequired(token)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
    }

    @Test
    @DisplayName("Should proceed when token is valid, password reset is required, but accessing public route")
    void doFilterInternal_WithValidTokenResetRequiredAndPublicRoute_ShouldProceed() throws Exception {
        // Arrange
        String token = "temp-token";
        String email = "newuser@company.com";
        User user = new User();
        user.setEmail(email);
        user.setPasswordResetRequired(true);
        user.setRole(br.com.effies.laboris.backend.domain.entity.enums.UserRole.EMPLOYEE);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(tokenService.validateToken(token)).thenReturn(email);
        when(tokenService.isPasswordResetRequired(token)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }
}
