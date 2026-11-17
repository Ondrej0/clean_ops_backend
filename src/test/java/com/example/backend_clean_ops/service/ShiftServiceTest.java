package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.Schedule;
import com.example.backend_clean_ops.enums.ShiftStatus;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.dto.request.CreateShiftRequest;
import com.example.backend_clean_ops.dto.request.UpdateShiftRequest;
import com.example.backend_clean_ops.repository.SiteRepository;
import com.example.backend_clean_ops.repository.ShiftRepository;
import com.example.backend_clean_ops.repository.TenantRepository;
import com.example.backend_clean_ops.repository.UserRepository;
import com.example.backend_clean_ops.repository.EmployeeFlagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShiftServiceTest {
    private final ShiftRepository shifts = mock(ShiftRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final TenantRepository tenants = mock(TenantRepository.class);
    private final SiteRepository sites = mock(SiteRepository.class);
    private final EmployeeFlagRepository flags = mock(EmployeeFlagRepository.class);
    // Already November 8 in the UK, although still November 7 in this clock's zone.
    private final ShiftService service = new ShiftService(shifts, users, tenants, sites, new ShiftTimingPolicy(),
            Clock.fixed(Instant.parse("2026-11-08T00:30:00Z"), ZoneId.of("America/New_York")), flags);
    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    private User employee() {
        User user = User.builder().tenant(tenant()).firstName("Alex").lastName("Smith").role(UserRole.CLEANER).active(true).build();
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private Tenant tenant() {
        Tenant tenant = Tenant.builder().name("Company").build();
        org.springframework.test.util.ReflectionTestUtils.setField(tenant, "id", tenantId);
        return tenant;
    }

    private Site site() {
        Site site = Site.builder().tenant(tenant()).name("Office").build();
        org.springframework.test.util.ReflectionTestUtils.setField(site, "id", UUID.randomUUID());
        return site;
    }

    @Test
    void explicitCalendarWeekUsesInclusiveDatesAndPreservesShiftIdentityAndScheduleName() {
        when(users.existsByIdAndTenantId(userId, tenantId)).thenReturn(true);
        var from = LocalDate.parse("2026-07-06");
        var to = LocalDate.parse("2026-07-12");
        Shift shift = Shift.builder().site(Site.builder().name("Office").build()).user(employee())
                .schedule(com.example.backend_clean_ops.entity.Schedule.builder().name("Morning clean").build())
                .scheduledStart(from.atTime(9, 0)).scheduledEnd(from.atTime(12, 0))
                .status(ShiftStatus.COMPLETED).build();
        org.springframework.test.util.ReflectionTestUtils.setField(shift, "id", UUID.randomUUID());
        when(shifts.findInWindow(tenantId, userId, from.atStartOfDay(), to.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(shift));
        var result = service.getShifts(tenantId, userId, from, to);
        assertEquals(from, result.fromDate());
        assertEquals(to, result.toDate());
        assertEquals(shift.getId(), result.shifts().getFirst().id());
        assertEquals(userId, result.shifts().getFirst().employee().id());
        assertEquals("Alex", result.shifts().getFirst().employee().firstName());
        assertEquals("Morning clean", result.shifts().getFirst().scheduleName());
        assertEquals(ZoneOffset.ofHours(1), result.shifts().getFirst().scheduledStart().getOffset());
        verify(shifts).findInWindow(tenantId, userId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    @Test
    void invalidCalendarRangesAreRejectedBeforeQueryingShifts() {
        var from = LocalDate.parse("2026-11-09");
        for (LocalDate[] range : List.of(new LocalDate[]{from, null}, new LocalDate[]{null, from},
                new LocalDate[]{from, from.minusDays(1)}, new LocalDate[]{from, from.plusDays(366)})) {
            var error = assertThrows(ResponseStatusException.class,
                    () -> service.getShifts(tenantId, userId, range[0], range[1]));
            assertEquals(400, error.getStatusCode().value());
        }
        verifyNoInteractions(shifts);
    }

    @Test
    void usesUkDatesAndIncludesHistoryWithNoActions() {
        when(users.existsByIdAndTenantId(userId, tenantId)).thenReturn(true);
        Shift completed = Shift.builder().site(Site.builder().name("Office").build()).user(employee())
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
    void siteWindowReturnsAllEmployeesAtOnlyTheSelectedSite() {
        UUID siteId = UUID.randomUUID();
        var from = LocalDate.parse("2026-11-09");
        var to = LocalDate.parse("2026-11-15");
        Shift shift = Shift.builder().site(Site.builder().name("Office").build()).user(employee())
                .scheduledStart(from.atTime(9, 0)).scheduledEnd(from.atTime(12, 0))
                .status(ShiftStatus.SCHEDULED).build();
        when(shifts.findBySiteInWindow(tenantId, siteId, from.atStartOfDay(), to.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(shift));
        var result = service.getShiftsBySite(tenantId, siteId, from, to);
        assertEquals(1, result.shifts().size());
        assertEquals(userId, result.shifts().getFirst().employee().id());
        verify(shifts).findBySiteInWindow(tenantId, siteId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    @Test
    void unknownUserOrWrongTenantReturns404WithoutReadingShifts() {
        var error = assertThrows(ResponseStatusException.class, () -> service.getShifts(tenantId, userId));
        assertEquals(404, error.getStatusCode().value());
        verifyNoInteractions(shifts);
    }

    @Test
    void createsStandaloneOneOffShiftForActiveTenantCleaner() {
        Site site = site();
        User employee = employee();
        var request = new CreateShiftRequest(site.getId(), userId,
                LocalDateTime.parse("2026-11-09T09:00"), LocalDateTime.parse("2026-11-09T12:00"));
        when(tenants.findById(tenantId)).thenReturn(Optional.of(tenant()));
        when(sites.findById(site.getId())).thenReturn(Optional.of(site));
        when(users.findById(userId)).thenReturn(Optional.of(employee));
        when(shifts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createShift(tenantId, request);
        var saved = (Shift) mockingDetails(shifts).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("save")).findFirst().orElseThrow().getArgument(0);
        assertNull(saved.getSchedule());
        assertTrue(saved.isManualOverride());
        assertEquals(request.scheduledStart().toLocalDate(), saved.getShiftDate());
        assertEquals("ONE_OFF", result.origin());
        assertTrue(result.managerEditable());
    }

    @Test
    void editingGeneratedOccurrencePreservesOriginAndDoesNotChangeSchedule() {
        User original = employee();
        User replacement = User.builder().tenant(tenant()).firstName("Sam").lastName("Jones")
                .role(UserRole.CLEANER).active(true).build();
        UUID replacementId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(replacement, "id", replacementId);
        Schedule schedule = Schedule.builder().name("Weekly clean").build();
        Shift shift = Shift.builder().tenant(tenant()).site(site()).user(original).schedule(schedule)
                .shiftDate(LocalDate.parse("2026-11-09")).scheduledStart(LocalDateTime.parse("2026-11-09T09:00"))
                .scheduledEnd(LocalDateTime.parse("2026-11-09T12:00")).status(ShiftStatus.SCHEDULED).build();
        UUID shiftId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(shift, "id", shiftId);
        when(shifts.findForManagerUpdate(shiftId)).thenReturn(Optional.of(shift));
        when(users.findById(replacementId)).thenReturn(Optional.of(replacement));
        when(shifts.save(shift)).thenReturn(shift);

        var result = service.updateShift(tenantId, shiftId, new UpdateShiftRequest(replacementId,
                LocalDateTime.parse("2026-11-10T10:00"), LocalDateTime.parse("2026-11-10T13:00")));
        assertSame(schedule, shift.getSchedule());
        assertEquals(userId, shift.getSourceUserId());
        assertEquals(LocalDateTime.parse("2026-11-09T09:00"), shift.getSourceScheduledStart());
        assertEquals(replacement, shift.getUser());
        assertTrue(shift.isManualOverride());
        assertEquals("ONE_OFF_OVERRIDE", result.origin());
    }

    @Test
    void removalCreatesHiddenExceptionAndAttendanceBearingShiftIsImmutable() {
        Shift shift = Shift.builder().tenant(tenant()).site(site()).user(employee())
                .shiftDate(LocalDate.parse("2026-11-09")).scheduledStart(LocalDateTime.parse("2026-11-09T09:00"))
                .scheduledEnd(LocalDateTime.parse("2026-11-09T12:00")).status(ShiftStatus.SCHEDULED).build();
        UUID shiftId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(shift, "id", shiftId);
        when(shifts.findForManagerUpdate(shiftId)).thenReturn(Optional.of(shift));
        service.removeShift(tenantId, shiftId);
        assertTrue(shift.isManualOverride());
        assertNotNull(shift.getRemovedAt());
        verify(shifts).save(shift);

        shift.setRemovedAt(null);
        shift.setActualStart(LocalDateTime.parse("2026-11-09T09:01"));
        var error = assertThrows(ResponseStatusException.class,
                () -> service.updateShift(tenantId, shiftId, new UpdateShiftRequest(userId,
                        LocalDateTime.parse("2026-11-10T09:00"), LocalDateTime.parse("2026-11-10T12:00"))));
        assertEquals(409, error.getStatusCode().value());
    }
}
