package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.AttendanceRequest;
import com.example.backend_clean_ops.dto.responses.AttendanceResponse;
import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.CleanerShiftStatus;
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
import java.util.Optional;

// Placeholder for attendance-related business rules and clocking workflows.
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final ShiftRepository shiftRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;

    //TODO Finish clock in and clock out flows
    @Transactional
    public AttendanceResponse clockIn(AttendanceRequest request) {

        Shift shift = shiftRepository.findById(request.shiftId())
                .orElseThrow(() -> new RuntimeException("Shift ID not found."));

        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new RuntimeException("Tenant ID not found."));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User ID not found."));

        Site site = siteRepository.findById(request.siteId())
                .orElseThrow(() -> new RuntimeException("Site ID not found."));

        if (!shift.getTenant().getId().equals(tenant.getId())) {
            throw new RuntimeException("Tenant does not match");
        }

        if (!shift.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("User does not match");
        }

        if (!shift.getSite().getId().equals(site.getId())) {
            throw new RuntimeException("Site does not match");
        }

        shift.setActualStart(request.clockedIn());
        shift.setStatus(ShiftStatus.IN_PROGRESS);

        shiftRepository.save(shift);

        return new AttendanceResponse(shift.getStatus());
    }

    @Transactional
    public AttendanceResponse clockOut(AttendanceRequest request) {

        Shift shift = shiftRepository.findById(request.shiftId())
                .orElseThrow(() -> new RuntimeException("Shift ID not found."));

        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new RuntimeException("Tenant ID not found."));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User ID not found."));

        Site site = siteRepository.findById(request.siteId())
                .orElseThrow(() -> new RuntimeException("Site ID not found."));

        if (!shift.getTenant().getId().equals(tenant.getId())) {
            throw new RuntimeException("Tenant does not match");
        }

        if (!shift.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("User does not match");
        }

        if (!shift.getSite().getId().equals(site.getId())) {
            throw new RuntimeException("Site does not match");
        }

        shift.setActualEnd(request.clockedIn());
        shift.setStatus(ShiftStatus.COMPLETED);

        shiftRepository.save(shift);

        return new AttendanceResponse(shift.getStatus());
    }
}
