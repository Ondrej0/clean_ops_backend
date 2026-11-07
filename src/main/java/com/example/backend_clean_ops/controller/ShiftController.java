package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.responses.GetShiftsResponse;
import com.example.backend_clean_ops.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {
    private final ShiftService shiftService;

    @GetMapping
    public GetShiftsResponse getShifts(@RequestParam UUID tenantId, @RequestParam UUID userId) {
        return shiftService.getShifts(tenantId, userId);
    }
}
