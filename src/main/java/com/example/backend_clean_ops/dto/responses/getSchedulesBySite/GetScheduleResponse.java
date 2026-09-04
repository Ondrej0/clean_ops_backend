package com.example.backend_clean_ops.dto.responses.getSchedulesBySite;

import java.time.LocalDateTime;
import java.util.List;

public record GetScheduleResponse(
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<GetScheduleRuleResponse> scheduleRules
) {
}
