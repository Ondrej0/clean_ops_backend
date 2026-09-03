package com.example.backend_clean_ops.dto.responses;

import java.util.UUID;

public record CleanerResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone
) {
}
