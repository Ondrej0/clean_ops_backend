package com.example.backend_clean_ops.dto.responses.getSchedulesBySite;

import com.example.backend_clean_ops.enums.DayOfWeek;

import java.time.LocalTime;
import java.util.UUID;

public record GetScheduleRuleResponse(
        UUID scheduleRuleId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}
