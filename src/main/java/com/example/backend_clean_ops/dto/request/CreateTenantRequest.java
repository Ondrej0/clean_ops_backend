package com.example.backend_clean_ops.dto.request;

public record CreateTenantRequest(
        String name,
        String contactName,
        String contactEmail,
        String addressLine1,
        String city,
        String postcode
) {}
