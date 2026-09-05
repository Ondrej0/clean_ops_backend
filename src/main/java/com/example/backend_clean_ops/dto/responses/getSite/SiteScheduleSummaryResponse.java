package com.example.backend_clean_ops.dto.responses.getSite;

import java.util.UUID;

public record SiteScheduleSummaryResponse(
        UUID scheduleId,
        String name
) {
}
