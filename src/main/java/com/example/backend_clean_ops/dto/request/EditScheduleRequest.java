package com.example.backend_clean_ops.dto.request;

import java.util.List;
import java.util.UUID;

public record EditScheduleRequest(
    UUID tenantID,
    UUID scheduleID,
    String name,
    List<ScheduleRuleRequest> scheduleRule
) {}
