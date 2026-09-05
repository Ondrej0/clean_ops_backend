package com.example.backend_clean_ops.dto.responses.getSite;

import com.example.backend_clean_ops.enums.SiteStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetSiteByIdResponse(
        UUID siteId,
        String name,
        String addressLine1,
        String addressLine2,
        String city,
        String postcode,
        String contactName,
        String contactPhone,
        String contactEmail,
        SiteStatus status,
        BigDecimal hourlyRate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SiteScheduleSummaryResponse> assignedSchedules,
        List<SiteCleanerSummaryResponse> assignedCleaners
) {
}
