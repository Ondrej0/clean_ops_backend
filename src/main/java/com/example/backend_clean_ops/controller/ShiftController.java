package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.responses.GetShiftsResponse;
import com.example.backend_clean_ops.service.ShiftService;
import com.example.backend_clean_ops.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {
    private final ResourceAccess access;
    private final ShiftService shiftService;

    @GetMapping
    public GetShiftsResponse getShifts(@RequestParam(required = false) UUID tenantId, @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        tenantId = access.tenant(tenantId);
        userId = access.employee(userId);
        if (userId == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Select an employee.");
        if (fromDate == null && toDate == null) return shiftService.getShifts(tenantId, userId);
        return shiftService.getShifts(tenantId, userId, fromDate, toDate);
    }
}
