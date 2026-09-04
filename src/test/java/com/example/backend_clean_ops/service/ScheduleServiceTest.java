package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateScheduleRequest;
import com.example.backend_clean_ops.dto.request.EditScheduleRequest;
import com.example.backend_clean_ops.dto.request.ScheduleRuleRequest;
import com.example.backend_clean_ops.dto.responses.CreateScheduleResponse;
import com.example.backend_clean_ops.dto.responses.getSchedule.GetScheduleByIdResponse;
import com.example.backend_clean_ops.dto.responses.getSchedulesBySite.GetSchedulesBySitesResponse;
import com.example.backend_clean_ops.entity.Schedule;
import com.example.backend_clean_ops.entity.ScheduleAssignment;
import com.example.backend_clean_ops.entity.ScheduleRule;
import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.DayOfWeek;
import com.example.backend_clean_ops.enums.ShiftStatus;
import com.example.backend_clean_ops.enums.SiteStatus;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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

    @Mock
    private ShiftRepository shiftRepository;

    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleService(
                scheduleRepository,
                scheduleRuleRepository,
                scheduleAssignmentRepository,
                userRepository,
                tenantRepository,
                siteRepository,
                shiftRepository
        );
    }

    @Test
    @DisplayName("Should return a schedule with its site, active rules, and assigned cleaners")
    void getSchedule_whenScheduleExists_shouldMapScheduleDetails() {
        UUID scheduleId = UUID.randomUUID();
        UUID scheduleRuleId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();
        UUID cleanerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 9, 2, 10, 0);
        Schedule schedule = mock(Schedule.class);
        ScheduleRule rule = mock(ScheduleRule.class);
        Site site = mock(Site.class);
        ScheduleAssignment assignment = mock(ScheduleAssignment.class);
        User cleaner = mock(User.class);

        when(scheduleRepository.findByIdAndActiveTrue(scheduleId)).thenReturn(Optional.of(schedule));
        when(schedule.getId()).thenReturn(scheduleId);
        when(schedule.getName()).thenReturn("Weekdays");
        when(schedule.getCreatedAt()).thenReturn(createdAt);
        when(schedule.getUpdatedAt()).thenReturn(updatedAt);
        when(schedule.getSite()).thenReturn(site);
        when(site.getId()).thenReturn(siteId);
        when(siteRepository.findByIdAndStatus(siteId, SiteStatus.ACTIVE)).thenReturn(Optional.of(site));
        when(site.getName()).thenReturn("Central Office");
        when(site.getPostcode()).thenReturn("SW1A 1AA");
        when(scheduleRuleRepository.findAllByScheduleIdAndActiveTrue(scheduleId)).thenReturn(List.of(rule));
        when(rule.getId()).thenReturn(scheduleRuleId);
        when(rule.getDayOfWeek()).thenReturn(DayOfWeek.MONDAY);
        when(rule.getStartTime()).thenReturn(LocalTime.of(9, 0));
        when(rule.getEndTime()).thenReturn(LocalTime.of(17, 0));
        when(scheduleAssignmentRepository.findByScheduleId(scheduleId)).thenReturn(List.of(assignment));
        when(assignment.getUser()).thenReturn(cleaner);
        when(cleaner.getId()).thenReturn(cleanerId);
        when(cleaner.getFirstName()).thenReturn("Alex");
        when(cleaner.getLastName()).thenReturn("Smith");
        when(cleaner.isActive()).thenReturn(true);

        GetScheduleByIdResponse response = scheduleService.getSchedule(scheduleId);

        assertAll(
                () -> assertEquals(scheduleId, response.scheduleId()),
                () -> assertEquals("Weekdays", response.name()),
                () -> assertEquals(createdAt, response.createdAt()),
                () -> assertEquals(updatedAt, response.updatedAt()),
                () -> assertEquals(siteId, response.site().siteId()),
                () -> assertEquals("Central Office", response.site().name()),
                () -> assertEquals("SW1A 1AA", response.site().postcode()),
                () -> assertEquals(1, response.scheduleRules().size()),
                () -> assertEquals(scheduleRuleId, response.scheduleRules().getFirst().scheduleRuleId()),
                () -> assertEquals(DayOfWeek.MONDAY, response.scheduleRules().getFirst().dayOfWeek()),
                () -> assertEquals(LocalTime.of(9, 0), response.scheduleRules().getFirst().startTime()),
                () -> assertEquals(LocalTime.of(17, 0), response.scheduleRules().getFirst().endTime()),
                () -> assertEquals(1, response.assignedCleaners().size()),
                () -> assertEquals(cleanerId, response.assignedCleaners().getFirst().cleanerId()),
                () -> assertEquals("Alex", response.assignedCleaners().getFirst().firstName()),
                () -> assertEquals("Smith", response.assignedCleaners().getFirst().lastName())
        );

        verify(scheduleRepository).findByIdAndActiveTrue(scheduleId);
        verify(siteRepository).findByIdAndStatus(siteId, SiteStatus.ACTIVE);
        verify(scheduleRuleRepository).findAllByScheduleIdAndActiveTrue(scheduleId);
        verify(scheduleAssignmentRepository).findByScheduleId(scheduleId);
        verifyNoMoreInteractions(scheduleRepository, scheduleRuleRepository, scheduleAssignmentRepository, siteRepository);
    }

    @Test
    @DisplayName("Should throw an exception when the schedule cannot be found")
    void getSchedule_whenScheduleDoesNotExist_shouldThrowException() {
        UUID scheduleId = UUID.randomUUID();
        when(scheduleRepository.findByIdAndActiveTrue(scheduleId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> scheduleService.getSchedule(scheduleId)
        );

        assertEquals("Schedule not found", exception.getMessage());
        verify(scheduleRepository).findByIdAndActiveTrue(scheduleId);
        verifyNoInteractions(scheduleRuleRepository);
        verifyNoInteractions(scheduleAssignmentRepository);
        verifyNoMoreInteractions(scheduleRepository);
    }

    @Test
    @DisplayName("Should return active sites with only their active schedules and rules")
    void getSchedulesBySites_shouldMapActiveHierarchy() {
        UUID tenantId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID scheduleRuleId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 9, 2, 10, 0);
        Site site = mock(Site.class);
        Schedule schedule = mock(Schedule.class);
        ScheduleRule rule = mock(ScheduleRule.class);

        when(siteRepository.findAllByTenantIdAndStatus(tenantId, SiteStatus.ACTIVE))
                .thenReturn(List.of(site));
        when(site.getId()).thenReturn(siteId);
        when(site.getName()).thenReturn("Central Office");
        when(site.getAddressLine1()).thenReturn("1 High Street");
        when(site.getCity()).thenReturn("London");
        when(site.getPostcode()).thenReturn("SW1A 1AA");
        when(scheduleRepository.findAllBySiteIdAndActiveTrue(siteId)).thenReturn(List.of(schedule));
        when(schedule.getId()).thenReturn(scheduleId);
        when(schedule.getName()).thenReturn("Weekdays");
        when(schedule.getCreatedAt()).thenReturn(createdAt);
        when(schedule.getUpdatedAt()).thenReturn(updatedAt);
        when(scheduleRuleRepository.findAllByScheduleIdAndActiveTrue(scheduleId)).thenReturn(List.of(rule));
        when(rule.getId()).thenReturn(scheduleRuleId);
        when(rule.getDayOfWeek()).thenReturn(DayOfWeek.MONDAY);
        when(rule.getStartTime()).thenReturn(LocalTime.of(9, 0));
        when(rule.getEndTime()).thenReturn(LocalTime.of(17, 0));

        List<GetSchedulesBySitesResponse> response = scheduleService.getSchedulesBySites(tenantId);

        assertEquals(1, response.size());
        GetSchedulesBySitesResponse siteResponse = response.getFirst();
        assertAll(
                () -> assertEquals(siteId, siteResponse.siteId()),
                () -> assertEquals("Central Office", siteResponse.siteName()),
                () -> assertEquals("1 High Street", siteResponse.addressLine1()),
                () -> assertEquals("London", siteResponse.city()),
                () -> assertEquals("SW1A 1AA", siteResponse.postcode()),
                () -> assertEquals(1, siteResponse.schedules().size()),
                () -> assertEquals(scheduleId, siteResponse.schedules().getFirst().scheduleId()),
                () -> assertEquals("Weekdays", siteResponse.schedules().getFirst().name()),
                () -> assertEquals(createdAt, siteResponse.schedules().getFirst().createdAt()),
                () -> assertEquals(updatedAt, siteResponse.schedules().getFirst().updatedAt()),
                () -> assertEquals(1, siteResponse.schedules().getFirst().scheduleRules().size()),
                () -> assertEquals(scheduleRuleId,
                        siteResponse.schedules().getFirst().scheduleRules().getFirst().scheduleRuleId()),
                () -> assertEquals(DayOfWeek.MONDAY,
                        siteResponse.schedules().getFirst().scheduleRules().getFirst().dayOfWeek()),
                () -> assertEquals(LocalTime.of(9, 0),
                        siteResponse.schedules().getFirst().scheduleRules().getFirst().startTime()),
                () -> assertEquals(LocalTime.of(17, 0),
                        siteResponse.schedules().getFirst().scheduleRules().getFirst().endTime())
        );

        verify(siteRepository).findAllByTenantIdAndStatus(tenantId, SiteStatus.ACTIVE);
        verify(scheduleRepository).findAllBySiteIdAndActiveTrue(siteId);
        verify(scheduleRuleRepository).findAllByScheduleIdAndActiveTrue(scheduleId);
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
                () -> assertEquals(scheduleId, response.scheduleId()),
                () -> assertEquals(createdSiteId, response.siteId()),
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
    @DisplayName("Should delete future scheduled shifts, replace rules, and regenerate shifts when editing a schedule")
    void editSchedule_whenScheduleExists_shouldReplaceRulesAndRegenerateShifts() {
        UUID tenantId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        Tenant tenant = mock(Tenant.class);
        Site site = mock(Site.class);
        Schedule schedule = mock(Schedule.class);
        User cleaner = mock(User.class);
        ScheduleAssignment assignment = mock(ScheduleAssignment.class);
        ScheduleRule newRule = mock(ScheduleRule.class);
        EditScheduleRequest request = new EditScheduleRequest(
                tenantId,
                scheduleId,
                "Updated Weekday Shift",
                List.of(new ScheduleRuleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(17, 0)
                ))
        );

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(schedule.getId()).thenReturn(scheduleId);
        when(schedule.getTenant()).thenReturn(tenant);
        when(schedule.getSite()).thenReturn(site);
        when(scheduleAssignmentRepository.findByScheduleId(scheduleId)).thenReturn(List.of(assignment));
        when(assignment.getUser()).thenReturn(cleaner);
        when(scheduleRuleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.of(List.of(newRule)));
        when(newRule.getDayOfWeek()).thenReturn(DayOfWeek.MONDAY);
        when(newRule.getStartTime()).thenReturn(LocalTime.of(9, 0));
        when(newRule.getEndTime()).thenReturn(LocalTime.of(17, 0));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scheduleService.editSchedule(request);

        ArgumentCaptor<ScheduleRule> ruleCaptor = ArgumentCaptor.forClass(ScheduleRule.class);
        ArgumentCaptor<Shift> shiftCaptor = ArgumentCaptor.forClass(Shift.class);

        verify(scheduleRepository).findById(scheduleId);
        verify(schedule).setName("Updated Weekday Shift");
        verify(shiftRepository).deleteByScheduleIdAndShiftDateGreaterThanEqualAndStatus(
                eq(scheduleId),
                eq(LocalDate.now()),
                eq(ShiftStatus.SCHEDULED)
        );
        verify(scheduleRuleRepository).deleteByScheduleId(scheduleId);
        verify(scheduleRuleRepository).save(ruleCaptor.capture());
        verify(scheduleRuleRepository).findByScheduleId(scheduleId);
        verify(scheduleAssignmentRepository).findByScheduleId(scheduleId);
        verify(shiftRepository, times(5)).save(shiftCaptor.capture());

        ScheduleRule savedRule = ruleCaptor.getValue();
        assertAll(
                () -> assertSame(tenant, savedRule.getTenant()),
                () -> assertSame(schedule, savedRule.getSchedule()),
                () -> assertEquals(DayOfWeek.MONDAY, savedRule.getDayOfWeek()),
                () -> assertEquals(LocalTime.of(9, 0), savedRule.getStartTime()),
                () -> assertEquals(LocalTime.of(17, 0), savedRule.getEndTime())
        );

        List<Shift> savedShifts = shiftCaptor.getAllValues();
        assertEquals(5, savedShifts.size());

        LocalDate expectedStartDate = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.MONDAY));
        for (int i = 0; i < savedShifts.size(); i++) {
            Shift shift = savedShifts.get(i);
            LocalDate expectedDate = expectedStartDate.plusWeeks(i);
            LocalDateTime expectedStart = LocalDateTime.of(expectedDate, LocalTime.of(9, 0));
            LocalDateTime expectedEnd = LocalDateTime.of(expectedDate, LocalTime.of(17, 0));

            assertAll(
                    () -> assertSame(tenant, shift.getTenant()),
                    () -> assertSame(site, shift.getSite()),
                    () -> assertSame(schedule, shift.getSchedule()),
                    () -> assertSame(cleaner, shift.getUser()),
                    () -> assertEquals(expectedDate, shift.getShiftDate()),
                    () -> assertEquals(expectedStart, shift.getScheduledStart()),
                    () -> assertEquals(expectedEnd, shift.getScheduledEnd()),
                    () -> assertEquals(ShiftStatus.SCHEDULED, shift.getStatus())
            );
        }

        verifyNoMoreInteractions(
                scheduleRepository,
                scheduleRuleRepository,
                scheduleAssignmentRepository,
                shiftRepository
        );
    }

    @Test
    @DisplayName("Should assign a cleaner and generate shifts for each schedule rule")
    void assignCleanerToSchedule_whenDataIsValid_shouldSaveAssignmentAndGenerateShifts() {
        UUID tenantId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID cleanerId = UUID.randomUUID();
        Tenant tenant = mock(Tenant.class);
        Tenant cleanerTenant = mock(Tenant.class);
        Schedule schedule = mock(Schedule.class);
        Site site = mock(Site.class);
        User cleaner = mock(User.class);
        ScheduleAssignment savedAssignment = mock(ScheduleAssignment.class);
        ScheduleRule weekdayRule = mock(ScheduleRule.class);
        ScheduleRule overnightRule = mock(ScheduleRule.class);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(userRepository.findById(cleanerId)).thenReturn(Optional.of(cleaner));
        when(cleaner.getRole()).thenReturn(UserRole.CLEANER);
        when(schedule.getId()).thenReturn(scheduleId);
        when(schedule.getTenant()).thenReturn(tenant);
        when(schedule.getSite()).thenReturn(site);
        when(tenant.getId()).thenReturn(tenantId);
        when(cleaner.getTenant()).thenReturn(cleanerTenant);
        when(cleanerTenant.getId()).thenReturn(tenantId);
        when(scheduleAssignmentRepository.existsByScheduleIdAndUserId(scheduleId, cleanerId)).thenReturn(false);
        when(scheduleAssignmentRepository.save(any(ScheduleAssignment.class))).thenReturn(savedAssignment);
        when(scheduleRuleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.of(List.of(weekdayRule, overnightRule)));
        when(weekdayRule.getDayOfWeek()).thenReturn(DayOfWeek.MONDAY);
        when(weekdayRule.getStartTime()).thenReturn(LocalTime.of(9, 0));
        when(weekdayRule.getEndTime()).thenReturn(LocalTime.of(17, 0));
        when(overnightRule.getDayOfWeek()).thenReturn(DayOfWeek.WEDNESDAY);
        when(overnightRule.getStartTime()).thenReturn(LocalTime.of(22, 0));
        when(overnightRule.getEndTime()).thenReturn(LocalTime.of(2, 0));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scheduleService.assignCleanerToSchedule(tenantId, scheduleId, cleanerId);

        ArgumentCaptor<ScheduleAssignment> assignmentCaptor = ArgumentCaptor.forClass(ScheduleAssignment.class);
        ArgumentCaptor<Shift> shiftCaptor = ArgumentCaptor.forClass(Shift.class);

        verify(tenantRepository).findById(tenantId);
        verify(scheduleRepository).findById(scheduleId);
        verify(userRepository).findById(cleanerId);
        verify(scheduleAssignmentRepository).existsByScheduleIdAndUserId(scheduleId, cleanerId);
        verify(scheduleAssignmentRepository).save(assignmentCaptor.capture());
        verify(scheduleRuleRepository).findByScheduleId(scheduleId);
        verify(shiftRepository, times(10)).save(shiftCaptor.capture());

        ScheduleAssignment savedAssignmentEntity = assignmentCaptor.getValue();
        assertAll(
                () -> assertSame(tenant, savedAssignmentEntity.getTenant()),
                () -> assertSame(schedule, savedAssignmentEntity.getSchedule()),
                () -> assertSame(cleaner, savedAssignmentEntity.getUser())
        );

        List<Shift> savedShifts = shiftCaptor.getAllValues();
        assertEquals(10, savedShifts.size());

        LocalDate today = LocalDate.now();
        LocalDate mondayStart = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate wednesdayStart = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.WEDNESDAY));

        for (int i = 0; i < 5; i++) {
            Shift shift = savedShifts.get(i);
            LocalDate expectedDate = mondayStart.plusWeeks(i);
            LocalDateTime expectedStart = LocalDateTime.of(expectedDate, LocalTime.of(9, 0));
            LocalDateTime expectedEnd = LocalDateTime.of(expectedDate, LocalTime.of(17, 0));

            assertAll(
                    () -> assertSame(tenant, shift.getTenant()),
                    () -> assertSame(site, shift.getSite()),
                    () -> assertSame(schedule, shift.getSchedule()),
                    () -> assertSame(cleaner, shift.getUser()),
                    () -> assertEquals(expectedDate, shift.getShiftDate()),
                    () -> assertEquals(expectedStart, shift.getScheduledStart()),
                    () -> assertEquals(expectedEnd, shift.getScheduledEnd()),
                    () -> assertEquals(ShiftStatus.SCHEDULED, shift.getStatus())
            );
        }

        for (int i = 5; i < 10; i++) {
            Shift shift = savedShifts.get(i);
            LocalDate expectedDate = wednesdayStart.plusWeeks(i - 5);
            LocalDateTime expectedStart = LocalDateTime.of(expectedDate, LocalTime.of(22, 0));
            LocalDateTime expectedEnd = LocalDateTime.of(expectedDate.plusDays(1), LocalTime.of(2, 0));

            assertAll(
                    () -> assertSame(tenant, shift.getTenant()),
                    () -> assertSame(site, shift.getSite()),
                    () -> assertSame(schedule, shift.getSchedule()),
                    () -> assertSame(cleaner, shift.getUser()),
                    () -> assertEquals(expectedDate, shift.getShiftDate()),
                    () -> assertEquals(expectedStart, shift.getScheduledStart()),
                    () -> assertEquals(expectedEnd, shift.getScheduledEnd()),
                    () -> assertEquals(ShiftStatus.SCHEDULED, shift.getStatus())
            );
        }

        verifyNoMoreInteractions(
                tenantRepository,
                scheduleRepository,
                userRepository,
                scheduleAssignmentRepository,
                scheduleRuleRepository,
                siteRepository,
                shiftRepository
        );
    }

    @Test
    @DisplayName("Should assign a cleaner without creating shifts when no schedule rules exist")
    void assignCleanerToSchedule_whenNoScheduleRulesExist_shouldSaveAssignmentOnly() {
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
        when(schedule.getId()).thenReturn(scheduleId);
        when(schedule.getTenant()).thenReturn(tenant);
        when(tenant.getId()).thenReturn(tenantId);
        when(cleaner.getTenant()).thenReturn(cleanerTenant);
        when(cleanerTenant.getId()).thenReturn(tenantId);
        when(scheduleAssignmentRepository.existsByScheduleIdAndUserId(scheduleId, cleanerId)).thenReturn(false);
        when(scheduleAssignmentRepository.save(any(ScheduleAssignment.class))).thenReturn(savedAssignment);
        when(scheduleRuleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.empty());

        scheduleService.assignCleanerToSchedule(tenantId, scheduleId, cleanerId);

        verify(tenantRepository).findById(tenantId);
        verify(scheduleRepository).findById(scheduleId);
        verify(userRepository).findById(cleanerId);
        verify(scheduleAssignmentRepository).existsByScheduleIdAndUserId(scheduleId, cleanerId);
        verify(scheduleAssignmentRepository).save(any(ScheduleAssignment.class));
        verify(scheduleRuleRepository).findByScheduleId(scheduleId);
        verifyNoInteractions(shiftRepository);
        verifyNoMoreInteractions(
                tenantRepository,
                scheduleRepository,
                userRepository,
                scheduleAssignmentRepository,
                scheduleRuleRepository,
                siteRepository,
                shiftRepository
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
