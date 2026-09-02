package com.example.backend_clean_ops.dto.responses;

import com.example.backend_clean_ops.enums.SiteStatus;

import java.util.UUID;

public record SiteResponse(
        UUID id,
        String name,
        String addressLine1,
        String city,
        String postcode,
        SiteStatus status
) {
}
