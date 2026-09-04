package com.example.backend_clean_ops.dto.responses.getSchedule;

import java.util.UUID;

public record AssignedCleanerResponse(
        UUID cleanerId,
        String firstName,
        String lastName
) {
}
