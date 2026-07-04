package com.example.backend_clean_ops.dto.request;

public record CreateSiteRequest(
        String name,
        String addressLine1,
        String city,
        String postcode
) {}