package br.com.effies.laboris.backend.presentation.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateJobRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private CreateJobRequestDto createValidDto() {
        CreateJobRequestDto dto = new CreateJobRequestDto();
        dto.setAddress("123 Street");
        dto.setLatitude(10.0);
        dto.setLongitude(20.0);
        dto.setClientName("Valid Client");
        dto.setBudget(new BigDecimal("1000.0"));
        dto.setBillingRate(new BigDecimal("150.0"));
        dto.setStartDate(LocalDate.now()); // Today
        dto.setResponsibleName("Responsible Person");
        dto.setResponsiblePhone("123456789");
        dto.setResponsibleEmail("responsible@example.com");
        return dto;
    }

    @Test
    @DisplayName("Should accept current date (today) as startDate")
    void whenStartDateIsToday_thenValidationSucceeds() {
        CreateJobRequestDto dto = createValidDto();
        dto.setStartDate(LocalDate.now());

        Set<ConstraintViolation<CreateJobRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should accept future date as startDate")
    void whenStartDateIsFuture_thenValidationSucceeds() {
        CreateJobRequestDto dto = createValidDto();
        dto.setStartDate(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<CreateJobRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject past date as startDate")
    void whenStartDateIsPast_thenValidationFails() {
        CreateJobRequestDto dto = createValidDto();
        dto.setStartDate(LocalDate.now().minusDays(1));

        Set<ConstraintViolation<CreateJobRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateJobRequestDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("startDate");
        assertThat(violation.getMessage()).isEqualTo("A data de início não pode ser no passado.");
    }
}
