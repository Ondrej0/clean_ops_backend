package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.responses.GetShiftsResponse;
import com.example.backend_clean_ops.dto.responses.GetShiftsResponse.ShiftDetails;
import com.example.backend_clean_ops.dto.responses.GetShiftsResponse.SiteDetails;
import com.example.backend_clean_ops.dto.responses.GetShiftsResponse.EmployeeDetails;
import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.repository.ShiftRepository;
import com.example.backend_clean_ops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftService {
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final ShiftTimingPolicy timing;
    private final Clock clock;

    @Transactional(readOnly = true)
    public GetShiftsResponse getShifts(UUID tenantId, UUID userId) {
        return getShifts(tenantId, userId, null, null);
    }

    @Transactional(readOnly = true)
    public GetShiftsResponse getShifts(UUID tenantId, UUID userId, LocalDate fromDate, LocalDate toDate) {
        validateRange(fromDate, toDate);
        if (!userRepository.existsByIdAndTenantId(userId, tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in tenant.");
        }
        return response(fromDate, toDate, (from, until) -> shiftRepository.findInWindow(tenantId, userId, from, until));
    }

    @Transactional(readOnly = true)
    public GetShiftsResponse getShiftsBySite(UUID tenantId, UUID siteId) {
        return getShiftsBySite(tenantId, siteId, null, null);
    }

    @Transactional(readOnly = true)
    public GetShiftsResponse getShiftsBySite(UUID tenantId, UUID siteId, LocalDate fromDate, LocalDate toDate) {
        validateRange(fromDate, toDate);
        return response(fromDate, toDate, (from, until) -> shiftRepository.findBySiteInWindow(tenantId, siteId, from, until));
    }

    private void validateRange(LocalDate fromDate, LocalDate toDate) {
        if ((fromDate == null) != (toDate == null)
                || (fromDate != null && (toDate.isBefore(fromDate)
                || java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) > 365
                || toDate.equals(LocalDate.MAX)))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide both dates in order, at most 366 days apart (inclusive).");
        }
    }

    private GetShiftsResponse response(LocalDate fromDate, LocalDate toDate, ShiftWindowQuery query) {
        Instant now = clock.instant();
        LocalDate today = now.atZone(ShiftTimingPolicy.ZONE).toLocalDate();
        LocalDate from = fromDate == null ? today.minusDays(3) : fromDate;
        LocalDate to = toDate == null ? today.plusDays(3) : toDate;
        var shifts = query.find(from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream().map(shift -> details(shift, now)).toList();
        return new GetShiftsResponse(ShiftTimingPolicy.ZONE.getId(), from, to, shifts);
    }

    @FunctionalInterface
    private interface ShiftWindowQuery {
        java.util.List<Shift> find(LocalDateTime from, LocalDateTime until);
    }

    private ShiftDetails details(Shift shift, Instant now) {
        return new ShiftDetails(shift.getId(), new SiteDetails(shift.getSite().getId(), shift.getSite().getName()),
                new EmployeeDetails(shift.getUser().getId(), shift.getUser().getFirstName(), shift.getUser().getLastName()),
                shift.getSchedule() == null ? null : shift.getSchedule().getName(),
                timing.timestamp(shift.getScheduledStart()), timing.timestamp(shift.getScheduledEnd()),
                timing.timestamp(shift.getActualStart()), timing.timestamp(shift.getActualEnd()), shift.getStatus(),
                timing.clockInOpensAt(shift), timing.clockInDeadline(shift), timing.clockOutDeadline(shift),
                timing.canClockIn(shift, now), timing.canClockOut(shift, now), timing.requiresManagerCorrection(shift, now));
    }
}
