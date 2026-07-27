package com.example.backend_clean_ops.dto.responses;

import com.example.backend_clean_ops.enums.ShiftStatus;

public record AttendanceResponse(
        ShiftStatus shiftStatus
) {
}
