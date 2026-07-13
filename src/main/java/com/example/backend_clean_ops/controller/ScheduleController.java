package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateScheduleRequest;
import com.example.backend_clean_ops.dto.responses.CreateScheduleResponse;
import com.example.backend_clean_ops.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Exposes schedule creation and cleaner-assignment endpoints.
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateScheduleResponse createAndAssignSchedule(@RequestBody CreateScheduleRequest createScheduleRequest) {
        return scheduleService.createAndAssignSchedule(createScheduleRequest);
    }

    @PostMapping("/assign/cleaner")
    @ResponseStatus(HttpStatus.CREATED)
    public void assignCleanerToSchedule(@RequestParam UUID tenantId, @RequestParam UUID scheduleId, @RequestParam UUID cleanerId) {
        scheduleService.assignCleanerToSchedule(tenantId, scheduleId, cleanerId);
    }
}
