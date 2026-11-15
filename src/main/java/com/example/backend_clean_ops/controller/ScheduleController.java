package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateScheduleRequest;
import com.example.backend_clean_ops.dto.request.EditScheduleRequest;
import com.example.backend_clean_ops.dto.responses.CreateScheduleResponse;
import com.example.backend_clean_ops.dto.responses.getSchedule.GetScheduleByIdResponse;
import com.example.backend_clean_ops.dto.responses.getSchedulesBySite.GetSchedulesBySitesResponse;
import com.example.backend_clean_ops.service.ScheduleService;
import com.example.backend_clean_ops.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Exposes schedule creation and cleaner-assignment endpoints.
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ResourceAccess access;

    private final ScheduleService scheduleService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public GetScheduleByIdResponse getSchedule(@RequestParam UUID scheduleId) {
        access.schedule(scheduleId);
        return scheduleService.getSchedule(scheduleId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateScheduleResponse createAndAssignSchedule(@RequestBody CreateScheduleRequest createScheduleRequest) {
        access.manager();
        access.site(createScheduleRequest.siteId());
        createScheduleRequest = new CreateScheduleRequest(access.tenant(createScheduleRequest.tenantId()), createScheduleRequest.siteId(), createScheduleRequest.name(), createScheduleRequest.scheduleRule());
        return scheduleService.createAndAssignSchedule(createScheduleRequest);
    }

    @PostMapping("/edit")
    @ResponseStatus(HttpStatus.OK)
    public void editSchedule(@RequestBody EditScheduleRequest request) {
        access.manager();
        access.schedule(request.scheduleId());
        request = new EditScheduleRequest(access.tenant(request.tenantId()), request.scheduleId(), request.name(), request.scheduleRule());
        scheduleService.editSchedule(request);
    }

    @PostMapping("/assign/cleaner")
    @ResponseStatus(HttpStatus.CREATED)
    public void assignCleanerToSchedule(@RequestParam(required = false) UUID tenantId, @RequestParam UUID scheduleId, @RequestParam UUID cleanerId) {
        access.manager();
        tenantId = access.tenant(tenantId);
        access.schedule(scheduleId);
        access.employee(cleanerId);
        scheduleService.assignCleanerToSchedule(tenantId, scheduleId, cleanerId);
    }

    @GetMapping("/by-site")
    @ResponseStatus(HttpStatus.OK)
    public List<GetSchedulesBySitesResponse> getSchedulesBySites(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID userId) {
        tenantId = access.tenant(tenantId);
        userId = access.employee(userId);
        return scheduleService.getSchedulesBySites(tenantId, userId);
    }


    //TODO DeactivateShedule
    //TODO Unassign Cleaner
}
