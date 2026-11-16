package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.enums.ShiftStatus;
import com.example.backend_clean_ops.repository.ShiftRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoShowServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Test
    void markExpiredUnstartedShifts_marksOnlyShiftsPastTheTwoHourGraceCutoff() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-16T14:00:00Z"), ZoneOffset.UTC);
        NoShowService service = new NoShowService(shiftRepository, new ShiftTimingPolicy(), clock);
        Shift expired = Shift.builder().scheduledStart(LocalDateTime.parse("2026-11-16T09:00:00"))
                .scheduledEnd(LocalDateTime.parse("2026-11-16T11:59:59")).status(ShiftStatus.SCHEDULED).build();
        Shift atDeadline = Shift.builder().scheduledStart(LocalDateTime.parse("2026-11-16T09:00:00"))
                .scheduledEnd(LocalDateTime.parse("2026-11-16T12:00:00")).status(ShiftStatus.SCHEDULED).build();
        when(shiftRepository.findUnstartedScheduledShiftsEndedBy(ShiftStatus.SCHEDULED,
                LocalDateTime.parse("2026-11-16T14:00:00"))).thenReturn(List.of(expired, atDeadline));

        int marked = service.markExpiredUnstartedShifts();

        assertEquals(1, marked);
        assertEquals(ShiftStatus.NO_SHOW, expired.getStatus());
        assertEquals(ShiftStatus.SCHEDULED, atDeadline.getStatus());
        verify(shiftRepository).findUnstartedScheduledShiftsEndedBy(ShiftStatus.SCHEDULED,
                LocalDateTime.parse("2026-11-16T14:00:00"));
    }

    @Test
    void markExpiredUnstartedShifts_usesTheUkDeadlineAcrossTheAutumnClockChange() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-25T02:31:00Z"), ZoneOffset.UTC);
        NoShowService service = new NoShowService(shiftRepository, new ShiftTimingPolicy(), clock);
        Shift expired = Shift.builder().scheduledStart(LocalDateTime.parse("2026-10-25T00:30:00"))
                .scheduledEnd(LocalDateTime.parse("2026-10-25T01:30:00")).status(ShiftStatus.SCHEDULED).build();
        when(shiftRepository.findUnstartedScheduledShiftsEndedBy(ShiftStatus.SCHEDULED,
                LocalDateTime.parse("2026-10-25T02:31:00"))).thenReturn(List.of(expired));

        assertEquals(1, service.markExpiredUnstartedShifts());
        assertEquals(ShiftStatus.NO_SHOW, expired.getStatus());
    }
}
