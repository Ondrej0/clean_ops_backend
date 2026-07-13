package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateScheduleRequest;
import com.example.backend_clean_ops.dto.request.ScheduleRuleRequest;
import com.example.backend_clean_ops.dto.responses.CreateScheduleResponse;
import com.example.backend_clean_ops.entity.Schedule;
import com.example.backend_clean_ops.entity.ScheduleAssignment;
import com.example.backend_clean_ops.entity.ScheduleRule;
import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.ShiftStatus;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Coordinates schedule creation, rule persistence, and shift generation.
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleRuleRepository scheduleRuleRepository;
    private final ScheduleAssignmentRepository scheduleAssignmentRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final SiteRepository siteRepository;
    private final ShiftRepository shiftRepository;

    public CreateScheduleResponse createAndAssignSchedule(CreateScheduleRequest request) {
        // Persist the schedule first so rules and generated shifts can reference it.
        Schedule schedule = new Schedule();
        Tenant tenant = tenantRepository.findById(request.tenantID())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Site site = siteRepository.findById(request.siteID())
                .orElseThrow(() -> new RuntimeException("Site not found"));

        schedule.setTenant(tenant);
        schedule.setSite(site);
        schedule.setName(request.name());

        Schedule savedSchedule = scheduleRepository.save(schedule);

        createAndAssignScheduleRuleToSchedule(request.scheduleRule(), savedSchedule, tenant);

        return new CreateScheduleResponse(
                savedSchedule.getId(),
                savedSchedule.getSite().getId(),
                savedSchedule.getCreatedAt()
        );
    }

    private void createAndAssignScheduleRuleToSchedule(
            List<ScheduleRuleRequest> scheduleRuleRequests,
            Schedule schedule,
            Tenant tenant
    ) {
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
    public void assignCleanerToSchedule(UUID tenantId, UUID scheduleId, UUID cleanerId) {
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

        if (scheduleAssignmentRepository.existsByScheduleIdAndUserId(scheduleId, cleanerId)) {
            throw new RuntimeException("Cleaner is already assigned to this schedule");
        }

        ScheduleAssignment scheduleAssignment = new ScheduleAssignment();

        scheduleAssignment.setTenant(tenant);
        scheduleAssignment.setSchedule(schedule);
        scheduleAssignment.setUser(cleaner);

        scheduleAssignmentRepository.save(scheduleAssignment);
        createShiftsForSchedule(schedule, cleaner, tenant);
    }

    private void createShiftsForSchedule(
            Schedule schedule,
            User cleaner,
            Tenant tenant
    ) {
        Optional<List<ScheduleRule>> scheduleRules =
                scheduleRuleRepository.findByScheduleId(schedule.getId());

        LocalDate today = LocalDate.now();

        if (scheduleRules.isEmpty()) {
            return;
        }

        for (ScheduleRule scheduleRule : scheduleRules.get()) {
            java.time.DayOfWeek javaDay =
                    java.time.DayOfWeek.valueOf(scheduleRule.getDayOfWeek().name());

            LocalDate firstShiftDate = today.with(
                    TemporalAdjusters.nextOrSame(javaDay)
            );

            // Pre-generate the next five weekly occurrences for each rule.
            for (int i = 0; i < 5; i++) {
                LocalDate shiftDate = firstShiftDate.plusWeeks(i);

                LocalDateTime scheduledStart = LocalDateTime.of(
                        shiftDate,
                        scheduleRule.getStartTime()
                );

                LocalDateTime scheduledEnd = LocalDateTime.of(
                        shiftDate,
                        scheduleRule.getEndTime()
                );

                // Supports overnight shifts, e.g. 22:00-02:00.
                if (!scheduledEnd.isAfter(scheduledStart)) {
                    scheduledEnd = scheduledEnd.plusDays(1);
                }

                Shift shift = new Shift();
                shift.setTenant(tenant);
                shift.setSite(schedule.getSite());
                shift.setSchedule(schedule);
                shift.setUser(cleaner);
                shift.setShiftDate(shiftDate);
                shift.setScheduledStart(scheduledStart);
                shift.setScheduledEnd(scheduledEnd);
                shift.setStatus(ShiftStatus.SCHEDULED);

                shiftRepository.save(shift);
            }
        }
    }
}
