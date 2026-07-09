package com.example.backend_clean_ops.dto.responses;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateSiteResponse(
        UUID siteId,
        String name,
        LocalDateTime createdAt
) {}