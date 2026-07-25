package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.AttendanceRequest;
import com.example.backend_clean_ops.dto.responses.AttendanceResponse;
import com.example.backend_clean_ops.enums.CleanerShiftStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// Placeholder for attendance-related business rules and clocking workflows.
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    //TODO Finish clock in and clock out flows
    public AttendanceResponse clockIn(AttendanceRequest request){
        return new AttendanceResponse();
    }

    public AttendanceResponse clockOut(AttendanceRequest request){
        return new AttendanceResponse();
    }
}
