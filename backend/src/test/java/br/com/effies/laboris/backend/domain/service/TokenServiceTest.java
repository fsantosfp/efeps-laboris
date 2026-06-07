package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private TokenService tokenService;
    private final String secret = "OoIkGTG6kpSDZNcuFKr2+dHKtA9ZjnUJcI1MU6Ie2Eo="; // 256-bit key

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", secret);
    }

    @Test
    @DisplayName("Should include passwordResetRequired=true claim when user requires password reset")
    void generateToken_WhenPasswordResetRequiredIsTrue_ShouldContainTrueClaim() {
        // Arrange
        User user = new User();
        user.setEmail("test@company.com");
        user.setRole(UserRole.EMPLOYEE);
        user.setPasswordResetRequired(true);

        // Act
        String token = tokenService.generateToken(user);

        // Assert
        Claims claims = getClaimsFromToken(token);
        assertThat(claims.getSubject()).isEqualTo("test@company.com");
        assertThat(claims.get("role")).isEqualTo("EMPLOYEE");
        assertThat(claims.get("passwordResetRequired", Boolean.class)).isTrue();
    }

    @Test
    @DisplayName("Should include passwordResetRequired=false claim when user does not require reset")
    void generateToken_WhenPasswordResetRequiredIsFalse_ShouldContainFalseClaim() {
        // Arrange
        User user = new User();
        user.setEmail("manager@company.com");
        user.setRole(UserRole.MANAGER);
        user.setPasswordResetRequired(false);

        // Act
        String token = tokenService.generateToken(user);

        // Assert
        Claims claims = getClaimsFromToken(token);
        assertThat(claims.getSubject()).isEqualTo("manager@company.com");
        assertThat(claims.get("role")).isEqualTo("MANAGER");
        assertThat(claims.get("passwordResetRequired", Boolean.class)).isFalse();
    }

    @Test
    @DisplayName("Should return subject (email) when token is valid")
    void validateToken_WhenValidToken_ShouldReturnEmail() {
        // Arrange
        User user = new User();
        user.setEmail("admin@laboris.com");
        user.setRole(UserRole.SAAS_OWNER);
        user.setPasswordResetRequired(false);
        String token = tokenService.generateToken(user);

        // Act
        String email = tokenService.validateToken(token);

        // Assert
        assertThat(email).isEqualTo("admin@laboris.com");
    }

    @Test
    @DisplayName("Should return empty string when token is invalid or expired")
    void validateToken_WhenInvalidToken_ShouldReturnEmptyString() {
        // Act
        String email = tokenService.validateToken("invalid-token-string");

        // Assert
        assertThat(email).isEmpty();
    }

    @Test
    @DisplayName("Should return true when token has passwordResetRequired claim set to true")
    void isPasswordResetRequired_WhenClaimIsTrue_ShouldReturnTrue() {
        // Arrange
        User user = new User();
        user.setEmail("reset@company.com");
        user.setRole(UserRole.EMPLOYEE);
        user.setPasswordResetRequired(true);
        String token = tokenService.generateToken(user);

        // Act
        boolean result = tokenService.isPasswordResetRequired(token);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when token has passwordResetRequired claim set to false")
    void isPasswordResetRequired_WhenClaimIsFalse_ShouldReturnFalse() {
        // Arrange
        User user = new User();
        user.setEmail("no-reset@company.com");
        user.setRole(UserRole.EMPLOYEE);
        user.setPasswordResetRequired(false);
        String token = tokenService.generateToken(user);

        // Act
        boolean result = tokenService.isPasswordResetRequired(token);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when token is invalid")
    void isPasswordResetRequired_WhenTokenIsInvalid_ShouldReturnFalse() {
        // Act
        boolean result = tokenService.isPasswordResetRequired("invalid-token-string");

        // Assert
        assertThat(result).isFalse();
    }

    private Claims getClaimsFromToken(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
