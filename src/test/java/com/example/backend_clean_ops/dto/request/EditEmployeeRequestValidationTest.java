package com.example.backend_clean_ops.dto.request;

import com.example.backend_clean_ops.enums.UserRole;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditEmployeeRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequest_shouldHaveNoValidationErrors() {
        EditEmployeeRequest request = new EditEmployeeRequest(
                UUID.randomUUID(), "Jamie", "Taylor", "jamie@example.com", "07123 456789",
                UserRole.CLEANER, new BigDecimal("16.50")
        );

        assertEquals(0, validator.validate(request).size());
    }

    @Test
    void invalidRequest_shouldRejectMissingRequiredValuesAndInvalidDetails() {
        EditEmployeeRequest request = new EditEmployeeRequest(
                null, "", "", "invalid-email", null, null, new BigDecimal("-0.01")
        );

        Set<String> invalidFields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(Set.of("tenantId", "firstName", "lastName", "email", "role", "payRate"), invalidFields);
    }
}
