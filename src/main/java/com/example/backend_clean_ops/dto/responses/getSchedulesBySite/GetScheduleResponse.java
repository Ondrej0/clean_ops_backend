package com.example.backend_clean_ops.dto.responses.getSchedulesBySite;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetScheduleResponse(
        UUID scheduleId,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<GetScheduleRuleResponse> scheduleRules
) {
}
