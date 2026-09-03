package com.example.backend_clean_ops.dto.responses;

import com.example.backend_clean_ops.enums.UserRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


//TODO Finish this, add aasigend sites, total hours worked, assigned shceduels, how late they are etc
public record GetCleanerDataResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        UserRole role,
        BigDecimal payRate,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
