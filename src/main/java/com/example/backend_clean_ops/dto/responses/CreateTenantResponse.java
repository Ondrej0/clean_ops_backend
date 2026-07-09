package com.example.backend_clean_ops.dto.responses;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTenantResponse(
        UUID tenantID,
        String name,
        LocalDateTime createdAt
) {
}
