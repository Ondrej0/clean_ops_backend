package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Reserved for attendance and clock-in endpoints as that workflow is expanded.
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
}
