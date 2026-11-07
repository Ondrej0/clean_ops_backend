package com.example.backend_clean_ops.dto.request;

import com.example.backend_clean_ops.enums.UserRole;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record EditEmployeeRequest(
        @NotNull UUID tenantId,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 50) String phone,
        @NotNull UserRole role,
        @DecimalMin(value = "0.00") @Digits(integer = 8, fraction = 2) BigDecimal payRate
) {
}
