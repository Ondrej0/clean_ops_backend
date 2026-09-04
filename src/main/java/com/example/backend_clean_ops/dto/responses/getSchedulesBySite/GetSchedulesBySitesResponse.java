package com.example.backend_clean_ops.dto.responses.getSchedulesBySite;

import java.util.List;
import java.util.UUID;

public record GetSchedulesBySitesResponse(
        UUID siteID,
        String siteName,
        String addressLine1,
        String city,
        String postcode,
        List<GetScheduleResponse> schedules
) {
}
