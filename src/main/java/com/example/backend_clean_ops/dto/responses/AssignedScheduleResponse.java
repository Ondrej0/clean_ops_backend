package com.example.backend_clean_ops.dto.responses;

import java.util.UUID;

public record AssignedScheduleResponse(
        UUID id,
        String name
) {
}
