package com.example.backend_clean_ops.dto.request;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AttendanceRequest(
        @NotNull UUID tenantId,
        @NotNull UUID siteId,
        @NotNull UUID shiftId,
        @NotNull UUID userId
) {}
