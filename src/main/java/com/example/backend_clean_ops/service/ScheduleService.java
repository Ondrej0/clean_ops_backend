package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateScheduleRequest;
import com.example.backend_clean_ops.dto.request.ScheduleRuleRequest;
import com.example.backend_clean_ops.dto.responses.CreateScheduleResponse;
import com.example.backend_clean_ops.entity.Schedule;
import com.example.backend_clean_ops.entity.ScheduleRule;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.repository.ScheduleRepository;
import com.example.backend_clean_ops.repository.ScheduleRuleRepository;
import com.example.backend_clean_ops.repository.SiteRepository;
import com.example.backend_clean_ops.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleRuleRepository  scheduleRuleRepository;
    private final TenantRepository tenantRepository;
    private final SiteRepository siteRepository;

    public CreateScheduleResponse createAndAssignSchedule(CreateScheduleRequest request)
    {
        Schedule schedule = new Schedule();
        Tenant tenant = tenantRepository.findById(request.tenantID())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Site site =  siteRepository.findById(request.siteID())
                .orElseThrow(() -> new RuntimeException("Site not found"));

        schedule.setTenant(tenant);
        schedule.setSite(site);
        schedule.setName(request.name());

        Schedule savedSchedule = scheduleRepository.save(schedule);

        createAndAssignScheduleRuleToSchedule(request.scheduleRule(), savedSchedule,tenant);

        return new  CreateScheduleResponse(
                savedSchedule.getId(),
                savedSchedule.getSite().getId(),
                savedSchedule.getCreatedAt()
        );

    }

    private void createAndAssignScheduleRuleToSchedule(List<ScheduleRuleRequest> scheduleRuleRequests, Schedule schedule, Tenant tenant) {
        for (ScheduleRuleRequest scheduleRuleRequest : scheduleRuleRequests) {
            ScheduleRule scheduleRuleEntity = new ScheduleRule();

            scheduleRuleEntity.setTenant(tenant);
            scheduleRuleEntity.setSchedule(schedule);
            scheduleRuleEntity.setDayOfWeek(scheduleRuleRequest.dayOfWeek());
            scheduleRuleEntity.setStartTime(scheduleRuleRequest.startTime());
            scheduleRuleEntity.setEndTime(scheduleRuleRequest.endTime());

            scheduleRuleRepository.save(scheduleRuleEntity);
        }
    }


}
