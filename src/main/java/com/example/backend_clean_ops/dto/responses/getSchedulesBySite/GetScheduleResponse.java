package com.example.backend_clean_ops.dto.responses.getSchedulesBySite;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetScheduleResponse(
        UUID scheduleID,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<GetScheduleRuleResponse> scheduleRules
) {
}
