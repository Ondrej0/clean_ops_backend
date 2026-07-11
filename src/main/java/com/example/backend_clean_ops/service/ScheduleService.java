package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateScheduleRequest;
import com.example.backend_clean_ops.dto.request.ScheduleRuleRequest;
import com.example.backend_clean_ops.dto.responses.CreateScheduleResponse;
import com.example.backend_clean_ops.entity.*;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleRuleRepository  scheduleRuleRepository;
    private final ScheduleAssignmentRepository scheduleAssignmentRepository;
    private final UserRepository userRepository;
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
    @Transactional
    public void assignCleanerToSchedule(UUID tenantId, UUID scheduleId, UUID cleanerId)
    {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        User cleaner = userRepository.findById(cleanerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (cleaner.getRole() != UserRole.CLEANER) {
            throw new RuntimeException("User is not a cleaner");
        }

        if (!schedule.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Schedule does not belong to tenant");
        }

        if (!cleaner.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Cleaner does not belong to tenant");
        }

        if (scheduleAssignmentRepository
                .existsByScheduleIdAndUserId(scheduleId, cleanerId)) {
            throw new RuntimeException("Cleaner is already assigned to this schedule");
        }

        ScheduleAssignment scheduleAssignment = new ScheduleAssignment();

        scheduleAssignment.setTenant(tenant);
        scheduleAssignment.setSchedule(schedule);
        scheduleAssignment.setUser(cleaner);

        scheduleAssignmentRepository.save(scheduleAssignment);
    }

    private void createShiftsForSchedule(UUID scheduleId, UUID cleanerId)
    {
        Optional<List<ScheduleRule>> scheduleRules = scheduleRuleRepository.findByScheduleId(scheduleId);

        if (scheduleRules.isPresent()) {
            for (ScheduleRule scheduleRule : scheduleRules.get()) {
                //TODO FINISH shift creation
            }
        }


    }


}
