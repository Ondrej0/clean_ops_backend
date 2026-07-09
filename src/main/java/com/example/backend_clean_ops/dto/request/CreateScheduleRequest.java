package com.example.backend_clean_ops.dto.request;

import java.util.List;
import java.util.UUID;

public record CreateScheduleRequest(
    UUID tenantID,
    UUID siteID,
    String name,
    List<ScheduleRule> scheduleRule
) {}
