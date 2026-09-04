package com.example.backend_clean_ops.dto.request;

import java.util.UUID;

public record CreateSiteRequest(
        UUID tenantId,
        String name,
        String addressLine1,
        String city,
        String postcode
) {}