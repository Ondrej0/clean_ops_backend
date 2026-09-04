package com.example.backend_clean_ops.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttendanceRequest(
        UUID tenantId,
        UUID siteId,
        UUID shiftId,
        UUID userId,
        LocalDateTime clockedIn
) {}
