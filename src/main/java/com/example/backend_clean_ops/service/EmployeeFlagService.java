package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.responses.EmployeeFlagResponse;
import com.example.backend_clean_ops.entity.*;
import com.example.backend_clean_ops.enums.*;
import com.example.backend_clean_ops.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service @RequiredArgsConstructor
public class EmployeeFlagService {
    private final EmployeeFlagRepository flags;
    private final UserRepository users;
    private final ShiftRepository shifts;
    private final CurrentEmployee current;
    private final ScheduleOverlapDetector detector;
    private final Clock clock;
    public record ReportResult(int detected, int created, int alreadyReported) {}

    @Transactional
    public ReportResult report(LocalDate from, LocalDate to) {
        User actor = current.require(UserRole.CLEANER);
        if (from == null || to == null || to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > 30
                || to.equals(LocalDate.MAX))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an ordered range of up to 31 days.");
        // Serialize reports for an employee, including requests with different/overlapping date ranges.
        User employee = users.lockForFlagReport(actor.getId(), actor.getTenant().getId())
                .filter(User::isActive).filter(u -> u.getRole() == UserRole.CLEANER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        UUID tenant = employee.getTenant().getId();
        var conflicts = detector.detect(shifts.findInWindow(tenant, employee.getId(),
                from.atStartOfDay(), to.plusDays(1).atStartOfDay()));
        int created = 0;
        for (var conflict : conflicts) {
            if (flags.existsByTenantIdAndEmployeeIdAndTypeAndOpenConflictKey(tenant, employee.getId(),
                    EmployeeFlagType.SCHEDULE_OVERLAP, conflict.key())) continue;
            EmployeeFlag flag = new EmployeeFlag();
            flag.setTenantId(tenant);
            flag.setEmployeeId(employee.getId());
            flag.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
            flag.setType(EmployeeFlagType.SCHEDULE_OVERLAP);
            flag.setStatus(EmployeeFlagStatus.OPEN);
            flag.setConflictKey(conflict.key());
            flag.setOpenConflictKey(conflict.key());
            flag.setFirstShiftId(conflict.first().getId());
            flag.setSecondShiftId(conflict.second().getId());
            flag.setMessage(describe(conflict.first()) + " overlaps with " + describe(conflict.second())
                    + ". Times are in " + ShiftTimingPolicy.ZONE.getId() + ".");
            flag.setCreatedAt(clock.instant());
            flags.save(flag);
            created++;
        }
        return new ReportResult(conflicts.size(), created, conflicts.size() - created);
    }

    @Transactional(readOnly = true)
    public List<EmployeeFlagResponse> list(UUID employeeId, EmployeeFlagStatus status) {
        User manager = current.require(UserRole.MANAGER);
        UUID tenant = manager.getTenant().getId();
        if (employeeId != null && !users.existsByIdAndTenantId(employeeId, tenant))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return flags.findFlags(tenant, employeeId, status).stream().map(EmployeeFlagResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeFlagResponse> mine(EmployeeFlagStatus status) {
        User employee = current.require(UserRole.CLEANER);
        return flags.findFlags(employee.getTenant().getId(), employee.getId(), status)
                .stream().map(EmployeeFlagResponse::from).toList();
    }

    @Transactional
    public EmployeeFlagResponse resolve(UUID id) {
        User manager = current.require(UserRole.MANAGER);
        EmployeeFlag flag = flags.findForResolution(id, manager.getTenant().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (flag.getStatus() == EmployeeFlagStatus.OPEN) {
            flag.setStatus(EmployeeFlagStatus.RESOLVED);
            flag.setResolvedAt(clock.instant());
            flag.setResolvedBy(manager.getId());
            flag.setOpenConflictKey(null);
        }
        return EmployeeFlagResponse.from(flag);
    }

    private String describe(Shift shift) {
        var format = DateTimeFormatter.ofPattern("EEE dd MMM uuuu HH:mm", Locale.UK);
        return shift.getSite().getName() + " · " + (shift.getSchedule() == null ? "Shift" : shift.getSchedule().getName())
                + " (" + shift.getScheduledStart().format(format) + " – " + shift.getScheduledEnd().format(format) + ")";
    }
}
