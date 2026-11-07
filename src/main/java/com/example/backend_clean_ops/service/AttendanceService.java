package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.AttendanceRequest;
import com.example.backend_clean_ops.dto.responses.AttendanceResponse;
import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.ShiftStatus;
import com.example.backend_clean_ops.repository.ShiftRepository;
import com.example.backend_clean_ops.repository.SiteRepository;
import com.example.backend_clean_ops.repository.TenantRepository;
import com.example.backend_clean_ops.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Clock;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final ShiftRepository shiftRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;

    private final ShiftTimingPolicy timing;
    private final Clock clock;
    @Transactional
    public AttendanceResponse clockIn(AttendanceRequest request) {

        Shift shift = shiftRepository.findForAttendance(request.shiftId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shift ID not found."));

        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant ID not found."));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User ID not found."));

        Site site = siteRepository.findById(request.siteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site ID not found."));

        if (!shift.getTenant().getId().equals(tenant.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant does not match");
        }

        if (!shift.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not match");
        }

        if (!shift.getSite().getId().equals(site.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Site does not match");
        }

        Instant now = clock.instant();
        if (!timing.canClockIn(shift, now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Shift cannot be clocked in at this time or in its current state.");
        }
        shift.setActualStart(LocalDateTime.ofInstant(now, ShiftTimingPolicy.ZONE));
        shift.setStatus(ShiftStatus.IN_PROGRESS);

        shiftRepository.save(shift);

        return new AttendanceResponse(shift.getStatus());
    }

    @Transactional
    public AttendanceResponse clockOut(AttendanceRequest request) {

        Shift shift = shiftRepository.findForAttendance(request.shiftId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shift ID not found."));

        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant ID not found."));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User ID not found."));

        Site site = siteRepository.findById(request.siteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site ID not found."));

        if (!shift.getTenant().getId().equals(tenant.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant does not match");
        }

        if (!shift.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not match");
        }

        if (!shift.getSite().getId().equals(site.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Site does not match");
        }

        Instant now = clock.instant();
        if (!timing.canClockOut(shift, now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    timing.requiresManagerCorrection(shift, now)
                            ? "Clock-out deadline passed or attendance is inconsistent. A manager must correct this shift."
                            : "Shift must be in progress with an existing clock-in and no clock-out.");
        }
        shift.setActualEnd(LocalDateTime.ofInstant(now, ShiftTimingPolicy.ZONE));
        shift.setStatus(ShiftStatus.COMPLETED);

        shiftRepository.save(shift);

        return new AttendanceResponse(shift.getStatus());
    }
}
