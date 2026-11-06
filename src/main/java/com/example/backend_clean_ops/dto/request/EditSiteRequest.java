package com.example.backend_clean_ops.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record EditSiteRequest(
        @NotNull UUID tenantId,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String addressLine1,
        @Size(max = 255) String addressLine2,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 20) String postcode,
        @Size(max = 255) String contactName,
        @Size(max = 50) String contactPhone,
        @Email @Size(max = 255) String contactEmail,
        @DecimalMin(value = "0.00") @Digits(integer = 8, fraction = 2) BigDecimal hourlyRate
) {
}
