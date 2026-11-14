package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.responses.GetShiftsResponse;
import com.example.backend_clean_ops.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {
    private final ShiftService shiftService;

    @GetMapping
    public GetShiftsResponse getShifts(@RequestParam UUID tenantId, @RequestParam UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        if (fromDate == null && toDate == null) return shiftService.getShifts(tenantId, userId);
        return shiftService.getShifts(tenantId, userId, fromDate, toDate);
    }
}
