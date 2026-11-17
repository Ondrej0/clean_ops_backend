package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.responses.GetShiftsResponse;
import com.example.backend_clean_ops.dto.responses.GetShiftsResponse.ShiftDetails;
import com.example.backend_clean_ops.dto.responses.GetShiftsResponse.SiteDetails;
import com.example.backend_clean_ops.dto.responses.GetShiftsResponse.EmployeeDetails;
import com.example.backend_clean_ops.dto.request.CreateShiftRequest;
import com.example.backend_clean_ops.dto.request.UpdateShiftRequest;
import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.entity.EmployeeFlag;
import com.example.backend_clean_ops.enums.ShiftStatus;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.repository.SiteRepository;
import com.example.backend_clean_ops.repository.ShiftRepository;
import com.example.backend_clean_ops.repository.TenantRepository;
import com.example.backend_clean_ops.repository.UserRepository;
import com.example.backend_clean_ops.repository.EmployeeFlagRepository;
import com.example.backend_clean_ops.enums.EmployeeFlagType;
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
    private final TenantRepository tenantRepository;
    private final SiteRepository siteRepository;
    private final ShiftTimingPolicy timing;
    private final Clock clock;
    private final EmployeeFlagRepository employeeFlags;

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
        return response(tenantId, fromDate, toDate, (from, until) -> shiftRepository.findInWindow(tenantId, userId, from, until));
    }

    @Transactional(readOnly = true)
    public GetShiftsResponse getShiftsBySite(UUID tenantId, UUID siteId) {
        return getShiftsBySite(tenantId, siteId, null, null);
    }

    @Transactional(readOnly = true)
    public GetShiftsResponse getShiftsBySite(UUID tenantId, UUID siteId, LocalDate fromDate, LocalDate toDate) {
        validateRange(fromDate, toDate);
        return response(tenantId, fromDate, toDate, (from, until) -> shiftRepository.findBySiteInWindow(tenantId, siteId, from, until));
    }

    @Transactional
    public ShiftDetails createShift(UUID tenantId, CreateShiftRequest request) {
        validateTimes(request.scheduledStart(), request.scheduledEnd());
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var site = siteRepository.findById(request.siteId())
                .filter(value -> value.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found in tenant."));
        User cleaner = cleaner(tenantId, request.userId());
        Shift shift = Shift.builder()
                .tenant(tenant).site(site).user(cleaner)
                .shiftDate(request.scheduledStart().toLocalDate())
                .scheduledStart(request.scheduledStart()).scheduledEnd(request.scheduledEnd())
                .status(ShiftStatus.SCHEDULED).manualOverride(true).build();
        return details(shiftRepository.save(shift), clock.instant());
    }

    @Transactional
    public ShiftDetails updateShift(UUID tenantId, UUID shiftId, UpdateShiftRequest request) {
        validateTimes(request.scheduledStart(), request.scheduledEnd());
        Shift shift = editableShift(tenantId, shiftId);
        User cleaner = cleaner(tenantId, request.userId());
        preserveOrigin(shift);
        shift.setUser(cleaner);
        shift.setShiftDate(request.scheduledStart().toLocalDate());
        shift.setScheduledStart(request.scheduledStart());
        shift.setScheduledEnd(request.scheduledEnd());
        shift.setManualOverride(true);
        return details(shiftRepository.save(shift), clock.instant());
    }

    @Transactional
    public void removeShift(UUID tenantId, UUID shiftId) {
        Shift shift = editableShift(tenantId, shiftId);
        preserveOrigin(shift);
        shift.setManualOverride(true);
        shift.setRemovedAt(LocalDateTime.ofInstant(clock.instant(), ShiftTimingPolicy.ZONE));
        shiftRepository.save(shift);
    }

    private User cleaner(UUID tenantId, UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getTenant().getId().equals(tenantId))
                .filter(User::isActive)
                .filter(user -> user.getRole() == UserRole.CLEANER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active cleaner not found in tenant."));
    }

    private Shift editableShift(UUID tenantId, UUID shiftId) {
        Shift shift = shiftRepository.findForManagerUpdate(shiftId)
                .filter(value -> value.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ShiftTimingPolicy.ZONE);
        if (shift.getStatus() != ShiftStatus.SCHEDULED || shift.getActualStart() != null
                || shift.getActualEnd() != null || !shift.getScheduledStart().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only future, unstarted scheduled shifts can be changed.");
        }
        return shift;
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)
                || Duration.between(start, end).compareTo(Duration.ofHours(24)) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shift end must be after start and no more than 24 hours later.");
        }
    }

    private void preserveOrigin(Shift shift) {
        if (shift.getSchedule() != null && shift.getSourceUserId() == null) {
            shift.setSourceUserId(shift.getUser().getId());
            shift.setSourceScheduledStart(shift.getScheduledStart());
            shift.setSourceScheduledEnd(shift.getScheduledEnd());
        }
    }

    private void validateRange(LocalDate fromDate, LocalDate toDate) {
        if ((fromDate == null) != (toDate == null)
                || (fromDate != null && (toDate.isBefore(fromDate)
                || java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) > 365
                || toDate.equals(LocalDate.MAX)))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide both dates in order, at most 366 days apart (inclusive).");
        }
    }

    private GetShiftsResponse response(UUID tenantId, LocalDate fromDate, LocalDate toDate, ShiftWindowQuery query) {
        Instant now = clock.instant();
        LocalDate today = now.atZone(ShiftTimingPolicy.ZONE).toLocalDate();
        LocalDate from = fromDate == null ? today.minusDays(3) : fromDate;
        LocalDate to = toDate == null ? today.plusDays(3) : toDate;
        var source = query.find(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
        var exceptionByShift = employeeFlags.findByTenantIdAndTypeAndFirstShiftIdIn(tenantId,
                        EmployeeFlagType.ATTENDANCE_EXCEPTION, source.stream().map(Shift::getId).toList())
                .stream().collect(java.util.stream.Collectors.toMap(EmployeeFlag::getFirstShiftId, flag -> flag,
                        (first, ignored) -> first));
        var shifts = source.stream().map(shift -> details(shift, now, exceptionByShift.get(shift.getId()))).toList();
        return new GetShiftsResponse(ShiftTimingPolicy.ZONE.getId(), from, to, shifts);
    }

    @FunctionalInterface
    private interface ShiftWindowQuery {
        java.util.List<Shift> find(LocalDateTime from, LocalDateTime until);
    }

    private ShiftDetails details(Shift shift, Instant now) { return details(shift, now, null); }

    private ShiftDetails details(Shift shift, Instant now, EmployeeFlag attendanceException) {
        String origin = shift.getSchedule() == null ? "ONE_OFF"
                : shift.isManualOverride() ? "ONE_OFF_OVERRIDE" : "SCHEDULED_OCCURRENCE";
        LocalDateTime localNow = LocalDateTime.ofInstant(now, ShiftTimingPolicy.ZONE);
        boolean managerEditable = shift.getStatus() == ShiftStatus.SCHEDULED && shift.getActualStart() == null
                && shift.getActualEnd() == null && shift.getRemovedAt() == null && shift.getScheduledStart().isAfter(localNow);
        return new ShiftDetails(shift.getId(), new SiteDetails(shift.getSite().getId(), shift.getSite().getName()),
                new EmployeeDetails(shift.getUser().getId(), shift.getUser().getFirstName(), shift.getUser().getLastName()),
                shift.getSchedule() == null ? null : shift.getSchedule().getName(),
                timing.timestamp(shift.getScheduledStart()), timing.timestamp(shift.getScheduledEnd()),
                timing.timestamp(shift.getActualStart()), timing.timestamp(shift.getActualEnd()), shift.getStatus(),
                timing.clockInOpensAt(shift), timing.clockInDeadline(shift), timing.clockOutDeadline(shift),
                timing.canClockIn(shift, now), timing.canClockOut(shift, now), timing.requiresManagerCorrection(shift, now),
                origin, managerEditable, attendanceException == null ? null
                        : new GetShiftsResponse.AttendanceException(attendanceException.getStatus().name(), attendanceException.getMessage()));
    }
}
