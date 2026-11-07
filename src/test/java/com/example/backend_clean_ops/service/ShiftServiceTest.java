package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.enums.ShiftStatus;
import com.example.backend_clean_ops.repository.ShiftRepository;
import com.example.backend_clean_ops.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShiftServiceTest {
    private final ShiftRepository shifts = mock(ShiftRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    // Already November 8 in the UK, although still November 7 in this clock's zone.
    private final ShiftService service = new ShiftService(shifts, users, new ShiftTimingPolicy(),
            Clock.fixed(Instant.parse("2026-11-08T00:30:00Z"), ZoneId.of("America/New_York")));
    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void usesUkDatesAndIncludesHistoryWithNoActions() {
        when(users.existsByIdAndTenantId(userId, tenantId)).thenReturn(true);
        Shift completed = Shift.builder().site(Site.builder().name("Office").build())
                .scheduledStart(LocalDateTime.parse("2026-11-07T09:00:00"))
                .scheduledEnd(LocalDateTime.parse("2026-11-07T12:00:00"))
                .status(ShiftStatus.COMPLETED).build();
        when(shifts.findInWindow(tenantId, userId, LocalDateTime.parse("2026-11-05T00:00:00"),
                LocalDateTime.parse("2026-11-12T00:00:00"))).thenReturn(List.of(completed));
        var result = service.getShifts(tenantId, userId);
        assertEquals("Europe/London", result.timezone());
        assertEquals(LocalDate.parse("2026-11-05"), result.fromDate());
        assertEquals(LocalDate.parse("2026-11-11"), result.toDate());
        assertEquals(1, result.shifts().size());
        assertEquals(ShiftStatus.COMPLETED, result.shifts().getFirst().status());
        assertFalse(result.shifts().getFirst().canClockIn());
        assertFalse(result.shifts().getFirst().canClockOut());
    }

    @Test
    void emptyWindowReturnsEmptyList() {
        when(users.existsByIdAndTenantId(userId, tenantId)).thenReturn(true);
        assertTrue(service.getShifts(tenantId, userId).shifts().isEmpty());
    }

    @Test
    void unknownUserOrWrongTenantReturns404WithoutReadingShifts() {
        var error = assertThrows(ResponseStatusException.class, () -> service.getShifts(tenantId, userId));
        assertEquals(404, error.getStatusCode().value());
        verifyNoInteractions(shifts);
    }
}
