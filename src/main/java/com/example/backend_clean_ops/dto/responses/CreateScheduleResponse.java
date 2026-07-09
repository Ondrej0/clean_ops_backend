package com.example.backend_clean_ops.dto.responses;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateScheduleResponse(
        UUID ScheduleID,
        UUID SiteID,
        LocalDateTime createdAt
) {
}
