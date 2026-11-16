package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.responses.GetShiftsResponse;
import com.example.backend_clean_ops.dto.request.CreateShiftRequest;
import com.example.backend_clean_ops.dto.request.UpdateShiftRequest;
import com.example.backend_clean_ops.service.ShiftService;
import com.example.backend_clean_ops.service.ResourceAccess;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            @RequestParam(required = false) UUID siteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        if (userId != null && siteId != null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Select either an employee or a site, not both.");
        }
        tenantId = access.tenant(tenantId);
        if (siteId != null) {
            access.manager();
            access.site(siteId);
            if (fromDate == null && toDate == null) return shiftService.getShiftsBySite(tenantId, siteId);
            return shiftService.getShiftsBySite(tenantId, siteId, fromDate, toDate);
        }
        userId = access.employee(userId);
        if (userId == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Select an employee.");
        if (fromDate == null && toDate == null) return shiftService.getShifts(tenantId, userId);
        return shiftService.getShifts(tenantId, userId, fromDate, toDate);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GetShiftsResponse.ShiftDetails createShift(@Valid @RequestBody CreateShiftRequest request) {
        access.manager();
        return shiftService.createShift(access.tenant(null), request);
    }

    @PutMapping("/{shiftId}")
    public GetShiftsResponse.ShiftDetails updateShift(@PathVariable UUID shiftId,
            @Valid @RequestBody UpdateShiftRequest request) {
        access.manager();
        return shiftService.updateShift(access.tenant(null), shiftId, request);
    }

    @DeleteMapping("/{shiftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeShift(@PathVariable UUID shiftId) {
        access.manager();
        shiftService.removeShift(access.tenant(null), shiftId);
    }
}
