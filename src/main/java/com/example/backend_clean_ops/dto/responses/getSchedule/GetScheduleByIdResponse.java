package com.example.backend_clean_ops.dto.responses.getSchedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetScheduleByIdResponse(
        UUID scheduleId,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ScheduleSiteResponse site,
        List<ScheduleRuleDetailsResponse> scheduleRules,
        List<AssignedCleanerResponse> assignedCleaners
) {
}
