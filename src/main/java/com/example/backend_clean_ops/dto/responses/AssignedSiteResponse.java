package com.example.backend_clean_ops.dto.responses;

import java.util.UUID;

public record AssignedSiteResponse(
        UUID id,
        String name,
        String addressLine1,
        String postcode
) {
}
