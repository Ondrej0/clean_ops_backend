package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.service.*;
import com.example.backend_clean_ops.dto.responses.GetShiftsResponse;
import com.example.backend_clean_ops.dto.responses.getSchedulesBySite.GetSchedulesBySitesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController @RequiredArgsConstructor
public class EmployeeSessionController {
    private final CurrentEmployee current;
    private final ShiftService shifts;
    private final ScheduleService schedules;
    public record SessionUser(UUID id, UUID tenantId, String name, UserRole role) {}

    @GetMapping("/api/session/csrf")
    public Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken()); }

    @GetMapping("/api/session/me") @Transactional(readOnly = true)
    public SessionUser me() {
        var user = current.get();
        return new SessionUser(user.getId(), user.getTenant().getId(),
                user.getFirstName() + " " + user.getLastName(), user.getRole());
    }

    @GetMapping("/api/me/shifts") @Transactional(readOnly = true)
    public GetShiftsResponse shifts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        var user = current.require(UserRole.CLEANER);
        return shifts.getShifts(user.getTenant().getId(), user.getId(), fromDate, toDate);
    }

    @GetMapping("/api/me/schedules") @Transactional(readOnly = true)
    public List<GetSchedulesBySitesResponse> schedules() {
        var user = current.require(UserRole.CLEANER);
        return schedules.getSchedulesBySites(user.getTenant().getId(), user.getId());
    }
}
