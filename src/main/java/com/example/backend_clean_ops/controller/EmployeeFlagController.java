package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.responses.EmployeeFlagResponse;
import com.example.backend_clean_ops.enums.EmployeeFlagStatus;
import com.example.backend_clean_ops.service.EmployeeFlagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/employee-flags") @RequiredArgsConstructor
public class EmployeeFlagController {
    private final EmployeeFlagService service;
    public record ReportRequest(@NotNull LocalDate fromDate, @NotNull LocalDate toDate,
            UUID employeeId, UUID userId, UUID tenantId) {}

    @PostMapping("/schedule-overlaps")
    public EmployeeFlagService.ReportResult report(@Valid @RequestBody ReportRequest request,
            @RequestParam Map<String, String> query) {
        // Explicitly reject identity overrides; reports always use the authenticated session.
        if (request.employeeId() != null || request.userId() != null || request.tenantId() != null
                || query.containsKey("employeeId") || query.containsKey("userId") || query.containsKey("tenantId"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identity overrides are not allowed.");
        return service.report(request.fromDate(), request.toDate());
    }

    @GetMapping
    public List<EmployeeFlagResponse> list(@RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) EmployeeFlagStatus status) { return service.list(employeeId, status); }

    @GetMapping("/mine")
    public List<EmployeeFlagResponse> mine(@RequestParam(required = false) EmployeeFlagStatus status) {
        return service.mine(status);
    }

    @PostMapping("/{id}/resolve")
    public EmployeeFlagResponse resolve(@PathVariable UUID id) { return service.resolve(id); }
}
