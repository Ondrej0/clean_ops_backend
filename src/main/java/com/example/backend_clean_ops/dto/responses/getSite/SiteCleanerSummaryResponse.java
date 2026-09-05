package com.example.backend_clean_ops.dto.responses.getSite;

import java.util.UUID;

public record SiteCleanerSummaryResponse(
        UUID cleanerId,
        String firstName,
        String lastName
) {
}
