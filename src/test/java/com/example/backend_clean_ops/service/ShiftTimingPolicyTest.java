package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.enums.ShiftStatus;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class ShiftTimingPolicyTest {
    private final ShiftTimingPolicy policy = new ShiftTimingPolicy();

    private Shift shift() {
        return Shift.builder().scheduledStart(LocalDateTime.parse("2026-11-07T09:00:00"))
                .scheduledEnd(LocalDateTime.parse("2026-11-07T12:00:00")).build();
    }

    @Test
    void clockInWindowIncludesBothBoundaries() {
        Shift shift = shift();
        Instant opens = Instant.parse("2026-11-07T08:30:00Z");
        Instant closes = Instant.parse("2026-11-07T14:00:00Z");
        assertFalse(policy.canClockIn(shift, opens.minusNanos(1)));
        assertTrue(policy.canClockIn(shift, opens));
        assertTrue(policy.canClockIn(shift, closes));
        assertFalse(policy.canClockIn(shift, closes.plusNanos(1)));
    }

    @Test
    void lateClockInMovesClockOutDeadlineAndExpiryRequiresManager() {
        Shift shift = shift();
        shift.setActualStart(LocalDateTime.parse("2026-11-07T13:00:00"));
        shift.setStatus(ShiftStatus.IN_PROGRESS);
        Instant deadline = Instant.parse("2026-11-07T18:00:00Z");
        assertEquals(deadline, policy.clockOutDeadline(shift).toInstant());
        assertTrue(policy.canClockOut(shift, deadline));
        assertFalse(policy.requiresManagerCorrection(shift, deadline));
        assertFalse(policy.canClockOut(shift, deadline.plusNanos(1)));
        assertTrue(policy.requiresManagerCorrection(shift, deadline.plusNanos(1)));
        assertFalse(policy.canClockIn(shift, Instant.parse("2026-11-07T13:01:00Z")));
        assertNull(shift.getActualEnd());
    }

    @Test
    void terminalStatusesAndMissingClockInDisallowActions() {
        Shift shift = shift();
        Instant now = Instant.parse("2026-11-07T10:00:00Z");
        assertNull(policy.clockOutDeadline(shift));
        assertFalse(policy.canClockOut(shift, now));
        for (ShiftStatus status : new ShiftStatus[]{ShiftStatus.COMPLETED, ShiftStatus.CANCELLED, ShiftStatus.NO_SHOW}) {
            shift.setStatus(status);
            assertFalse(policy.canClockIn(shift, now));
            assertFalse(policy.canClockOut(shift, now));
        }
        shift.setStatus(ShiftStatus.IN_PROGRESS);
        assertTrue(policy.requiresManagerCorrection(shift, now));
        assertFalse(policy.canClockOut(shift, now));
    }

    @Test
    void completedAttendanceCannotBeClockedOutAgain() {
        Shift shift = shift();
        shift.setStatus(ShiftStatus.IN_PROGRESS);
        shift.setActualStart(shift.getScheduledStart());
        shift.setActualEnd(shift.getScheduledEnd());
        assertFalse(policy.canClockOut(shift, Instant.parse("2026-11-07T12:00:00Z")));
    }

    @Test
    void overnightSpringClockChangeUsesElapsedDuration() {
        Shift shift = shift();
        shift.setScheduledStart(LocalDateTime.parse("2026-03-28T23:00:00"));
        shift.setScheduledEnd(LocalDateTime.parse("2026-03-29T04:00:00"));
        shift.setActualStart(LocalDateTime.parse("2026-03-29T04:00:00"));
        assertEquals(OffsetDateTime.parse("2026-03-29T10:00:00+01:00"), policy.clockOutDeadline(shift));
    }
}
