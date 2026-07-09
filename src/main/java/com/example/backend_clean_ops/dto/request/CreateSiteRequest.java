package com.example.backend_clean_ops.dto.request;

import java.util.UUID;

public record CreateSiteRequest(
        UUID tenantID,
        String name,
        String addressLine1,
        String city,
        String postcode
) {}