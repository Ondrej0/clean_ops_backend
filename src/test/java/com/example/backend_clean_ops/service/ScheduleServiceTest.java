package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateScheduleRequest;
import com.example.backend_clean_ops.dto.request.ScheduleRuleRequest;
import com.example.backend_clean_ops.dto.responses.CreateScheduleResponse;
import com.example.backend_clean_ops.entity.Schedule;
import com.example.backend_clean_ops.entity.ScheduleRule;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.enums.DayOfWeek;
import com.example.backend_clean_ops.repository.ScheduleRepository;
import com.example.backend_clean_ops.repository.ScheduleRuleRepository;
import com.example.backend_clean_ops.repository.SiteRepository;
import com.example.backend_clean_ops.repository.TenantRepository;
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
    private TenantRepository tenantRepository;

    @Mock
    private SiteRepository siteRepository;

    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleService(
                scheduleRepository,
                scheduleRuleRepository,
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
}
