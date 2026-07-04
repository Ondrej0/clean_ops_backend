package com.example.backend_clean_ops.dto.responses;

import com.example.backend_clean_ops.enums.SiteStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SiteResponse(
        UUID id,
        String name,
        String addressLine1,
        String addressLine2,
        String city,
        String postcode,
        String contactName,
        String contactPhone,
        String contactEmail,
        SiteStatus status,
        BigDecimal hourlyRate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}