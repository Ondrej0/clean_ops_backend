package com.example.backend_clean_ops.dto.responses;

import com.example.backend_clean_ops.entity.EmployeeFlag;
import com.example.backend_clean_ops.enums.*;
import java.time.Instant;
import java.util.UUID;

public record EmployeeFlagResponse(UUID id, UUID employeeId, String employeeName, EmployeeFlagType type,
        EmployeeFlagStatus status, String message, Instant createdAt, Instant resolvedAt, UUID resolvedBy,
        UUID firstShiftId, UUID secondShiftId) {
    public static EmployeeFlagResponse from(EmployeeFlag f) {
        return new EmployeeFlagResponse(f.getId(), f.getEmployeeId(), f.getEmployeeName(), f.getType(),
                f.getStatus(), f.getMessage(), f.getCreatedAt(), f.getResolvedAt(), f.getResolvedBy(),
                f.getFirstShiftId(), f.getSecondShiftId());
    }
}
