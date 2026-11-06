package com.example.backend_clean_ops.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditSiteRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequest_shouldHaveNoValidationErrors() {
        EditSiteRequest request = new EditSiteRequest(
                UUID.randomUUID(), "Central Depot", "12 High Street", "Suite 4", "London", "SW1A 1AA",
                "Pat Smith", "07123 456789", "pat@example.com", new BigDecimal("24.50")
        );

        assertEquals(0, validator.validate(request).size());
    }

    @Test
    void invalidRequest_shouldRejectMissingRequiredValuesAndInvalidContactDetails() {
        EditSiteRequest request = new EditSiteRequest(
                null, "", "", null, "", "", null, null, "invalid-email", new BigDecimal("-0.01")
        );

        Set<String> invalidFields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(Set.of("tenantId", "name", "addressLine1", "city", "postcode", "contactEmail", "hourlyRate"), invalidFields);
    }
}
