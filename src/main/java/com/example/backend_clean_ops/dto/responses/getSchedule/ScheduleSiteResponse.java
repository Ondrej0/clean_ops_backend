package com.example.backend_clean_ops.dto.responses.getSchedule;

import java.util.UUID;

public record ScheduleSiteResponse(
        UUID siteId,
        String name,
        String postcode
) {
}
