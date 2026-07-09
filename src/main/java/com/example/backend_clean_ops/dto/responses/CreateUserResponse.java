package com.example.backend_clean_ops.dto.responses;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateUserResponse(
        UUID userID,
        String firstName,
        String lastName,
        LocalDateTime createdAt
) {
}
