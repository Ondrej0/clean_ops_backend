package com.example.backend_clean_ops.dto.request;

import java.util.UUID;

public record GetSitesRequest(
        UUID tenantID
) {
}
