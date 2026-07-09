package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateScheduleRequest;
import com.example.backend_clean_ops.dto.responses.CreateScheduleResponse;
import com.example.backend_clean_ops.entity.Schedule;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.repository.ScheduleRepository;
import com.example.backend_clean_ops.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TenantRepository tenantRepository;

    public CreateScheduleResponse createAndAssignSchedule(CreateScheduleRequest request)
    {
        Schedule schedule = new Schedule();
        Tenant tenant = tenantRepository.findById(request.tenantID())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        //TODO finish schedule service


    }
}
