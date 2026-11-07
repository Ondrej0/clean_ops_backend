package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.enums.ShiftStatus;
import org.springframework.stereotype.Component;
import java.time.*;

/** Shared eligibility rules. Stored local timestamps are interpreted as UK time. */
@Component
public class ShiftTimingPolicy {
    public static final ZoneId ZONE = ZoneId.of("Europe/London");

    public OffsetDateTime timestamp(LocalDateTime value) {
        return value == null ? null : value.atZone(ZONE).toOffsetDateTime();
    }

    public OffsetDateTime clockInOpensAt(Shift shift) {
        return timestamp(shift.getScheduledStart()).minusMinutes(30);
    }

    public OffsetDateTime clockInDeadline(Shift shift) {
        return timestamp(shift.getScheduledEnd()).plusHours(2);
    }

    public OffsetDateTime clockOutDeadline(Shift shift) {
        if (shift.getActualStart() == null) return null;
        Duration duration = Duration.between(timestamp(shift.getScheduledStart()), timestamp(shift.getScheduledEnd()));
        return timestamp(shift.getActualStart()).plus(duration).plusHours(2);
    }

    public boolean canClockIn(Shift shift, Instant now) {
        return shift.getStatus() == ShiftStatus.SCHEDULED
                && shift.getActualStart() == null && shift.getActualEnd() == null
                && !now.isBefore(clockInOpensAt(shift).toInstant())
                && !now.isAfter(clockInDeadline(shift).toInstant());
    }

    public boolean canClockOut(Shift shift, Instant now) {
        return shift.getStatus() == ShiftStatus.IN_PROGRESS
                && shift.getActualStart() != null && shift.getActualEnd() == null
                && !now.isBefore(timestamp(shift.getActualStart()).toInstant())
                && !now.isAfter(clockOutDeadline(shift).toInstant());
    }

    public boolean requiresManagerCorrection(Shift shift, Instant now) {
        return shift.getStatus() == ShiftStatus.IN_PROGRESS && shift.getActualEnd() == null
                && (shift.getActualStart() == null || now.isAfter(clockOutDeadline(shift).toInstant()));
    }
}
