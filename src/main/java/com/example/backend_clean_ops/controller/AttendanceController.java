package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.AttendanceRequest;
import com.example.backend_clean_ops.dto.responses.AttendanceResponse;
import com.example.backend_clean_ops.service.AttendanceService;
import com.example.backend_clean_ops.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Reserved for attendance and clock-in endpoints as that workflow is expanded.
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final ResourceAccess access;

    private final AttendanceService attendanceService;

    @PostMapping("/clockIn")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AttendanceResponse clockIn(@Valid @RequestBody AttendanceRequest request){
        request = new AttendanceRequest(access.tenant(request.tenantId()), request.siteId(), request.shiftId(), access.cleaner(request.userId()));
        return attendanceService.clockIn(request);
    }

    @PostMapping("/clockOut")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AttendanceResponse clockOut(@Valid @RequestBody AttendanceRequest request){
        request = new AttendanceRequest(access.tenant(request.tenantId()), request.siteId(), request.shiftId(), access.cleaner(request.userId()));
        return attendanceService.clockOut(request);
    }

}
