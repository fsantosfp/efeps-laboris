package br.com.effies.laboris.backend.domain.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordGeneratorTest {

    @Test
    @DisplayName("Should generate a password of exactly 10 characters")
    void shouldGeneratePasswordOfLengthTen() {
        String password = PasswordGenerator.generate();
        assertThat(password).hasSize(10);
    }

    @Test
    @DisplayName("Should generate alphanumeric-only passwords")
    void shouldGenerateOnlyAlphanumericCharacters() {
        String password = PasswordGenerator.generate();
        assertThat(password).matches("^[a-zA-Z0-9]{10}$");
    }

    @Test
    @DisplayName("Should generate different passwords in consecutive calls")
    void shouldGenerateDifferentPasswordsConsecutively() {
        String password1 = PasswordGenerator.generate();
        String password2 = PasswordGenerator.generate();
        assertThat(password1).isNotEqualTo(password2);
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when trying to instantiate")
    void shouldThrowExceptionWhenInstantiating() throws Exception {
        java.lang.reflect.Constructor<PasswordGenerator> constructor = PasswordGenerator.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        org.junit.jupiter.api.Assertions.assertThrows(
            java.lang.reflect.InvocationTargetException.class,
            constructor::newInstance
        );
    }
}
