package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateScheduleRequest;
import com.example.backend_clean_ops.dto.request.ScheduleRuleRequest;
import com.example.backend_clean_ops.dto.responses.CreateScheduleResponse;
import com.example.backend_clean_ops.entity.Schedule;
import com.example.backend_clean_ops.entity.ScheduleAssignment;
import com.example.backend_clean_ops.entity.ScheduleRule;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.DayOfWeek;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleRuleRepository scheduleRuleRepository;

    @Mock
    private ScheduleAssignmentRepository scheduleAssignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private SiteRepository siteRepository;

    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleService(
                scheduleRepository,
                scheduleRuleRepository,
                scheduleAssignmentRepository,
                userRepository,
                tenantRepository,
                siteRepository
        );
    }

    @Test
    @DisplayName("Should save schedule and schedule rules, then return the created response")
    void createAndAssignSchedule_whenTenantAndSiteExist_shouldSaveScheduleAndRules() {
        UUID tenantId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID createdSiteId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 11, 11, 0);
        Tenant tenant = mock(Tenant.class);
        Site site = mock(Site.class);
        Schedule savedSchedule = mock(Schedule.class);
        ScheduleRuleRequest mondayRule = new ScheduleRuleRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );
        ScheduleRuleRequest tuesdayRule = new ScheduleRuleRequest(
                DayOfWeek.TUESDAY,
                LocalTime.of(10, 0),
                LocalTime.of(18, 0)
        );
        CreateScheduleRequest request = new CreateScheduleRequest(
                tenantId,
                siteId,
                "Weekday Shift",
                List.of(mondayRule, tuesdayRule)
        );

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(siteRepository.findById(siteId)).thenReturn(Optional.of(site));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(savedSchedule);
        when(savedSchedule.getId()).thenReturn(scheduleId);
        when(savedSchedule.getSite()).thenReturn(site);
        when(site.getId()).thenReturn(createdSiteId);
        when(savedSchedule.getCreatedAt()).thenReturn(createdAt);

        CreateScheduleResponse response = scheduleService.createAndAssignSchedule(request);

        assertAll(
                () -> assertEquals(scheduleId, response.ScheduleID()),
                () -> assertEquals(createdSiteId, response.SiteID()),
                () -> assertEquals(createdAt, response.createdAt())
        );

        ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
        ArgumentCaptor<ScheduleRule> ruleCaptor = ArgumentCaptor.forClass(ScheduleRule.class);

        verify(tenantRepository).findById(tenantId);
        verify(siteRepository).findById(siteId);
        verify(scheduleRepository).save(scheduleCaptor.capture());
        verify(scheduleRuleRepository, times(2)).save(ruleCaptor.capture());

        Schedule savedScheduleEntity = scheduleCaptor.getValue();
        assertAll(
                () -> assertSame(tenant, savedScheduleEntity.getTenant()),
                () -> assertSame(site, savedScheduleEntity.getSite()),
                () -> assertEquals("Weekday Shift", savedScheduleEntity.getName())
        );

        List<ScheduleRule> savedRules = ruleCaptor.getAllValues();
        assertEquals(2, savedRules.size());

        assertAll(
                () -> assertSame(tenant, savedRules.get(0).getTenant()),
                () -> assertSame(savedSchedule, savedRules.get(0).getSchedule()),
                () -> assertEquals(DayOfWeek.MONDAY, savedRules.get(0).getDayOfWeek()),
                () -> assertEquals(LocalTime.of(9, 0), savedRules.get(0).getStartTime()),
                () -> assertEquals(LocalTime.of(17, 0), savedRules.get(0).getEndTime())
        );
        assertAll(
                () -> assertSame(tenant, savedRules.get(1).getTenant()),
                () -> assertSame(savedSchedule, savedRules.get(1).getSchedule()),
                () -> assertEquals(DayOfWeek.TUESDAY, savedRules.get(1).getDayOfWeek()),
                () -> assertEquals(LocalTime.of(10, 0), savedRules.get(1).getStartTime()),
                () -> assertEquals(LocalTime.of(18, 0), savedRules.get(1).getEndTime())
        );

        verifyNoMoreInteractions(scheduleRepository, scheduleRuleRepository, tenantRepository, siteRepository);
    }

    @Test
    @DisplayName("Should throw when tenant cannot be found")
    void createAndAssignSchedule_whenTenantMissing_shouldThrowException() {
        UUID tenantId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();
        CreateScheduleRequest request = new CreateScheduleRequest(
                tenantId,
                siteId,
                "Weekday Shift",
                List.of()
        );

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> scheduleService.createAndAssignSchedule(request)
        );

        assertEquals("Tenant not found", exception.getMessage());

        verify(tenantRepository).findById(tenantId);
        verifyNoInteractions(siteRepository, scheduleRepository, scheduleRuleRepository);
        verifyNoMoreInteractions(tenantRepository);
    }

    @Test
    @DisplayName("Should assign a cleaner to a schedule when all validations pass")
    void assignCleanerToSchedule_whenDataIsValid_shouldSaveAssignment() {
        UUID tenantId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID cleanerId = UUID.randomUUID();
        Tenant tenant = mock(Tenant.class);
        Tenant cleanerTenant = mock(Tenant.class);
        Schedule schedule = mock(Schedule.class);
        User cleaner = mock(User.class);
        ScheduleAssignment savedAssignment = mock(ScheduleAssignment.class);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(userRepository.findById(cleanerId)).thenReturn(Optional.of(cleaner));
        when(cleaner.getRole()).thenReturn(UserRole.CLEANER);
        when(schedule.getTenant()).thenReturn(tenant);
        when(tenant.getId()).thenReturn(tenantId);
        when(cleaner.getTenant()).thenReturn(cleanerTenant);
        when(cleanerTenant.getId()).thenReturn(tenantId);
        when(scheduleAssignmentRepository.existsByScheduleIdAndUserId(scheduleId, cleanerId)).thenReturn(false);
        when(scheduleAssignmentRepository.save(any(ScheduleAssignment.class))).thenReturn(savedAssignment);

        scheduleService.assignCleanerToSchedule(tenantId, scheduleId, cleanerId);

        ArgumentCaptor<ScheduleAssignment> assignmentCaptor = ArgumentCaptor.forClass(ScheduleAssignment.class);

        verify(tenantRepository).findById(tenantId);
        verify(scheduleRepository).findById(scheduleId);
        verify(userRepository).findById(cleanerId);
        verify(scheduleAssignmentRepository).existsByScheduleIdAndUserId(scheduleId, cleanerId);
        verify(scheduleAssignmentRepository).save(assignmentCaptor.capture());

        ScheduleAssignment savedAssignmentEntity = assignmentCaptor.getValue();
        assertAll(
                () -> assertSame(tenant, savedAssignmentEntity.getTenant()),
                () -> assertSame(schedule, savedAssignmentEntity.getSchedule()),
                () -> assertSame(cleaner, savedAssignmentEntity.getUser())
        );

        verifyNoMoreInteractions(
                tenantRepository,
                scheduleRepository,
                userRepository,
                scheduleAssignmentRepository,
                scheduleRuleRepository,
                siteRepository
        );
    }

    @Test
    @DisplayName("Should reject assigning a user who is not a cleaner")
    void assignCleanerToSchedule_whenUserIsNotCleaner_shouldThrowException() {
        UUID tenantId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID cleanerId = UUID.randomUUID();
        Tenant tenant = mock(Tenant.class);
        Schedule schedule = mock(Schedule.class);
        User user = mock(User.class);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(userRepository.findById(cleanerId)).thenReturn(Optional.of(user));
        when(user.getRole()).thenReturn(UserRole.MANAGER);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> scheduleService.assignCleanerToSchedule(tenantId, scheduleId, cleanerId)
        );

        assertEquals("User is not a cleaner", exception.getMessage());

        verify(tenantRepository).findById(tenantId);
        verify(scheduleRepository).findById(scheduleId);
        verify(userRepository).findById(cleanerId);
        verify(user).getRole();
        verifyNoInteractions(scheduleAssignmentRepository);
        verifyNoMoreInteractions(tenantRepository, scheduleRepository, userRepository);
    }

    @Test
    @DisplayName("Should reject assigning a cleaner when the schedule belongs to another tenant")
    void assignCleanerToSchedule_whenScheduleBelongsToDifferentTenant_shouldThrowException() {
        UUID tenantId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID cleanerId = UUID.randomUUID();
        Tenant requestTenant = mock(Tenant.class);
        Tenant scheduleTenant = mock(Tenant.class);
        User cleaner = mock(User.class);
        Schedule schedule = mock(Schedule.class);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(requestTenant));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(userRepository.findById(cleanerId)).thenReturn(Optional.of(cleaner));
        when(cleaner.getRole()).thenReturn(UserRole.CLEANER);
        when(schedule.getTenant()).thenReturn(scheduleTenant);
        when(scheduleTenant.getId()).thenReturn(UUID.randomUUID());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> scheduleService.assignCleanerToSchedule(tenantId, scheduleId, cleanerId)
        );

        assertEquals("Schedule does not belong to tenant", exception.getMessage());

        verify(tenantRepository).findById(tenantId);
        verify(scheduleRepository).findById(scheduleId);
        verify(userRepository).findById(cleanerId);
        verify(cleaner).getRole();
        verify(schedule).getTenant();
        verify(scheduleTenant).getId();
        verifyNoInteractions(scheduleAssignmentRepository);
        verifyNoMoreInteractions(tenantRepository, scheduleRepository, userRepository);
    }

    @Test
    @DisplayName("Should reject duplicate cleaner assignment")
    void assignCleanerToSchedule_whenAlreadyAssigned_shouldThrowException() {
        UUID tenantId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID cleanerId = UUID.randomUUID();
        Tenant tenant = mock(Tenant.class);
        User cleaner = mock(User.class);
        Schedule schedule = mock(Schedule.class);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(userRepository.findById(cleanerId)).thenReturn(Optional.of(cleaner));
        when(cleaner.getRole()).thenReturn(UserRole.CLEANER);
        when(schedule.getTenant()).thenReturn(tenant);
        when(tenant.getId()).thenReturn(tenantId);
        when(cleaner.getTenant()).thenReturn(tenant);
        when(scheduleAssignmentRepository.existsByScheduleIdAndUserId(scheduleId, cleanerId)).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> scheduleService.assignCleanerToSchedule(tenantId, scheduleId, cleanerId)
        );

        assertEquals("Cleaner is already assigned to this schedule", exception.getMessage());

        verify(tenantRepository).findById(tenantId);
        verify(scheduleRepository).findById(scheduleId);
        verify(userRepository).findById(cleanerId);
        verify(scheduleAssignmentRepository).existsByScheduleIdAndUserId(scheduleId, cleanerId);
        verifyNoMoreInteractions(tenantRepository, scheduleRepository, userRepository, scheduleAssignmentRepository);
    }
}
