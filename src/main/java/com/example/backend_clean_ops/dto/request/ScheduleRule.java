package com.example.backend_clean_ops.dto.request;

import com.example.backend_clean_ops.enums.DayOfWeek;

import java.time.LocalTime;


public record ScheduleRule(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}
