package com.example.backend_clean_ops.dto.responses;

import com.example.backend_clean_ops.enums.ShiftStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record GetShiftsResponse(String timezone, LocalDate fromDate, LocalDate toDate, List<ShiftDetails> shifts) {
    public record SiteDetails(UUID id, String name) {}

    public record ShiftDetails(
            UUID id, SiteDetails site, String scheduleName,
            OffsetDateTime scheduledStart, OffsetDateTime scheduledEnd,
            OffsetDateTime actualStart, OffsetDateTime actualEnd, ShiftStatus status,
            OffsetDateTime clockInOpensAt, OffsetDateTime clockInDeadline,
            OffsetDateTime clockOutDeadline,
            boolean canClockIn, boolean canClockOut, boolean requiresManagerCorrection
    ) {}
}
