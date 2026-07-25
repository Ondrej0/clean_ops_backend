package com.example.backend_clean_ops.dto.responses;

import com.example.backend_clean_ops.enums.CleanerShiftStatus;

import java.time.LocalDateTime;

public record AttendanceResponse(
        CleanerShiftStatus cleanerShiftStatus,
        LocalDateTime timeRegistered
) {
}
