package com.example.backend_clean_ops.dto.responses.getSchedule;

import com.example.backend_clean_ops.enums.DayOfWeek;

import java.time.LocalTime;
import java.util.UUID;

public record ScheduleRuleDetailsResponse(
        UUID scheduleRuleId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}
