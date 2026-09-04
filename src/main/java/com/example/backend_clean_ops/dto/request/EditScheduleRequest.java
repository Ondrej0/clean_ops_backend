package com.example.backend_clean_ops.dto.request;

import java.util.List;
import java.util.UUID;

public record EditScheduleRequest(
    UUID tenantId,
    UUID scheduleId,
    String name,
    List<ScheduleRuleRequest> scheduleRule
) {}
