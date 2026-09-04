package com.example.backend_clean_ops.dto.responses.getSchedulesBySite;

import com.example.backend_clean_ops.enums.DayOfWeek;

import java.time.LocalTime;

public record GetScheduleRuleResponse(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}
