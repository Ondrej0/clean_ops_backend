package com.example.backend_clean_ops.dto.responses;

import com.example.backend_clean_ops.enums.UserRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public record GetCleanerDataResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        UserRole role,
        BigDecimal payRate,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AssignedScheduleResponse> assignedSchedules,
        List<AssignedSiteResponse> assignedSites
) {
}
