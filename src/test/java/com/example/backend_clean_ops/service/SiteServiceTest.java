package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateSiteRequest;
import com.example.backend_clean_ops.dto.responses.CreateSiteResponse;
import com.example.backend_clean_ops.dto.responses.GetSitesResponse;
import com.example.backend_clean_ops.dto.responses.SiteResponse;
import com.example.backend_clean_ops.dto.responses.getSite.GetSiteByIdResponse;
import com.example.backend_clean_ops.dto.responses.getSite.SiteCleanerSummaryResponse;
import com.example.backend_clean_ops.dto.responses.getSite.SiteScheduleSummaryResponse;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Schedule;
import com.example.backend_clean_ops.entity.ScheduleAssignment;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.SiteStatus;
import com.example.backend_clean_ops.repository.ScheduleAssignmentRepository;
import com.example.backend_clean_ops.repository.ScheduleRepository;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SiteServiceTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleAssignmentRepository scheduleAssignmentRepository;

    private SiteService siteService;

    @BeforeEach
    void setUp() {
        siteService = new SiteService(
                siteRepository,
                tenantRepository,
                scheduleRepository,
                scheduleAssignmentRepository
        );
    }

    @Test
    @DisplayName("Should map site request, save the site, and return the created response")
    void createSite_whenTenantExists_shouldSaveSiteAndReturnResponse() {
        UUID tenantId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 11, 11, 30);
        Tenant tenant = mock(Tenant.class);
        Site savedSite = mock(Site.class);
        CreateSiteRequest request = new CreateSiteRequest(
                tenantId,
                "Central Depot",
                "12 High Street",
                "London",
                "SW1A 1AA"
        );

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(siteRepository.save(any(Site.class))).thenReturn(savedSite);
        when(savedSite.getId()).thenReturn(siteId);
        when(savedSite.getName()).thenReturn("Central Depot");
        when(savedSite.getCreatedAt()).thenReturn(createdAt);

        CreateSiteResponse response = siteService.createSite(request);

        assertAll(
                () -> assertEquals(siteId, response.siteId()),
                () -> assertEquals("Central Depot", response.name()),
                () -> assertEquals(createdAt, response.createdAt())
        );

        ArgumentCaptor<Site> siteCaptor = ArgumentCaptor.forClass(Site.class);
        verify(tenantRepository).findById(tenantId);
        verify(siteRepository).save(siteCaptor.capture());

        Site savedSiteEntity = siteCaptor.getValue();
        assertAll(
                () -> assertSame(tenant, savedSiteEntity.getTenant()),
                () -> assertEquals("Central Depot", savedSiteEntity.getName()),
                () -> assertEquals("12 High Street", savedSiteEntity.getAddressLine1()),
                () -> assertEquals("London", savedSiteEntity.getCity()),
                () -> assertEquals("SW1A 1AA", savedSiteEntity.getPostcode())
        );

        verifyNoMoreInteractions(siteRepository, tenantRepository);
    }

    @Test
    @DisplayName("Should throw when tenant cannot be found")
    void createSite_whenTenantMissing_shouldThrowException() {
        UUID tenantId = UUID.randomUUID();
        CreateSiteRequest request = new CreateSiteRequest(
                tenantId,
                "Central Depot",
                "12 High Street",
                "London",
                "SW1A 1AA"
        );

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> siteService.createSite(request)
        );

        assertEquals("Tenant not found", exception.getMessage());

        verify(tenantRepository).findById(tenantId);
        verifyNoInteractions(siteRepository);
        verifyNoMoreInteractions(tenantRepository);
    }

    @Test
    @DisplayName("Should return sites mapped into site responses for tenant")
    void getSites_whenSitesExist_shouldReturnMappedSiteResponses() {
        UUID tenantId = UUID.randomUUID();
        UUID firstSiteId = UUID.randomUUID();
        Site firstSite = mock(Site.class);

        when(siteRepository.findAllByTenantIdAndStatus(tenantId, SiteStatus.ACTIVE)).thenReturn(List.of(firstSite));
        when(firstSite.getId()).thenReturn(firstSiteId);
        when(firstSite.getName()).thenReturn("Central Depot");
        when(firstSite.getAddressLine1()).thenReturn("12 High Street");
        when(firstSite.getCity()).thenReturn("London");
        when(firstSite.getPostcode()).thenReturn("SW1A 1AA");
        when(firstSite.getStatus()).thenReturn(SiteStatus.ACTIVE);
        GetSitesResponse response = siteService.getSites(tenantId);

        assertEquals(List.of(
                new SiteResponse(firstSiteId, "Central Depot", "12 High Street", "London", "SW1A 1AA", SiteStatus.ACTIVE)
        ), response.Sites());

        verify(siteRepository).findAllByTenantIdAndStatus(tenantId, SiteStatus.ACTIVE);
        verifyNoInteractions(tenantRepository);
        verifyNoMoreInteractions(siteRepository);
    }

    @Test
    @DisplayName("Should return an empty site list when tenant has no sites")
    void getSites_whenNoSitesExist_shouldReturnEmptySiteResponses() {
        UUID tenantId = UUID.randomUUID();
        when(siteRepository.findAllByTenantIdAndStatus(tenantId, SiteStatus.ACTIVE)).thenReturn(List.of());

        GetSitesResponse response = siteService.getSites(tenantId);

        assertTrue(response.Sites().isEmpty());

        verify(siteRepository).findAllByTenantIdAndStatus(tenantId, SiteStatus.ACTIVE);
        verifyNoInteractions(tenantRepository);
        verifyNoMoreInteractions(siteRepository);
    }

    @Test
    @DisplayName("Should return active site details with schedules and unique cleaners derived from schedule assignments")
    void getSiteById_whenSiteExists_shouldMapDetailsSchedulesAndScheduleAssignedCleaners() {
        UUID siteId = UUID.randomUUID();
        UUID firstScheduleId = UUID.randomUUID();
        UUID secondScheduleId = UUID.randomUUID();
        UUID cleanerId = UUID.randomUUID();
        UUID secondCleanerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 2, 10, 0);
        Site site = mock(Site.class);
        Schedule firstSchedule = mock(Schedule.class);
        Schedule secondSchedule = mock(Schedule.class);
        ScheduleAssignment firstAssignment = mock(ScheduleAssignment.class);
        ScheduleAssignment duplicateCleanerAssignment = mock(ScheduleAssignment.class);
        ScheduleAssignment secondAssignment = mock(ScheduleAssignment.class);
        User firstCleaner = mock(User.class);
        User secondCleaner = mock(User.class);

        when(siteRepository.findByIdAndStatus(siteId, SiteStatus.ACTIVE)).thenReturn(Optional.of(site));
        when(scheduleRepository.findAllBySiteIdAndActiveTrue(siteId))
                .thenReturn(List.of(firstSchedule, secondSchedule));
        when(scheduleAssignmentRepository.findBySiteIdWithActiveScheduleAndActiveUser(siteId))
                .thenReturn(List.of(firstAssignment, duplicateCleanerAssignment, secondAssignment));

        when(site.getId()).thenReturn(siteId);
        when(site.getName()).thenReturn("Central Depot");
        when(site.getAddressLine1()).thenReturn("12 High Street");
        when(site.getAddressLine2()).thenReturn("Suite 4");
        when(site.getCity()).thenReturn("London");
        when(site.getPostcode()).thenReturn("SW1A 1AA");
        when(site.getContactName()).thenReturn("Pat Smith");
        when(site.getContactPhone()).thenReturn("07123 456789");
        when(site.getContactEmail()).thenReturn("pat@example.com");
        when(site.getStatus()).thenReturn(SiteStatus.ACTIVE);
        when(site.getHourlyRate()).thenReturn(new BigDecimal("24.50"));
        when(site.getCreatedAt()).thenReturn(createdAt);
        when(site.getUpdatedAt()).thenReturn(updatedAt);
        when(firstSchedule.getId()).thenReturn(firstScheduleId);
        when(firstSchedule.getName()).thenReturn("Weekday mornings");
        when(secondSchedule.getId()).thenReturn(secondScheduleId);
        when(secondSchedule.getName()).thenReturn("Weekend cover");
        when(firstAssignment.getUser()).thenReturn(firstCleaner);
        when(duplicateCleanerAssignment.getUser()).thenReturn(firstCleaner);
        when(secondAssignment.getUser()).thenReturn(secondCleaner);
        when(firstCleaner.getId()).thenReturn(cleanerId);
        when(firstCleaner.getFirstName()).thenReturn("Alex");
        when(firstCleaner.getLastName()).thenReturn("Jones");
        when(secondCleaner.getId()).thenReturn(secondCleanerId);
        when(secondCleaner.getFirstName()).thenReturn("Sam");
        when(secondCleaner.getLastName()).thenReturn("Lee");

        GetSiteByIdResponse response = siteService.getSiteById(siteId);

        assertAll(
                () -> assertEquals(siteId, response.siteId()),
                () -> assertEquals("Suite 4", response.addressLine2()),
                () -> assertEquals(createdAt, response.createdAt()),
                () -> assertEquals(updatedAt, response.updatedAt()),
                () -> assertEquals(List.of(
                        new SiteScheduleSummaryResponse(firstScheduleId, "Weekday mornings"),
                        new SiteScheduleSummaryResponse(secondScheduleId, "Weekend cover")
                ), response.assignedSchedules()),
                () -> assertEquals(List.of(
                        new SiteCleanerSummaryResponse(cleanerId, "Alex", "Jones"),
                        new SiteCleanerSummaryResponse(secondCleanerId, "Sam", "Lee")
                ), response.assignedCleaners())
        );

        verify(siteRepository).findByIdAndStatus(siteId, SiteStatus.ACTIVE);
        verify(scheduleRepository).findAllBySiteIdAndActiveTrue(siteId);
        verify(scheduleAssignmentRepository).findBySiteIdWithActiveScheduleAndActiveUser(siteId);
    }

    @Test
    @DisplayName("Should throw when the requested site is not active or does not exist")
    void getSiteById_whenSiteMissing_shouldThrowException() {
        UUID siteId = UUID.randomUUID();
        when(siteRepository.findByIdAndStatus(siteId, SiteStatus.ACTIVE)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> siteService.getSiteById(siteId));

        assertEquals("Site not found", exception.getMessage());
        verify(siteRepository).findByIdAndStatus(siteId, SiteStatus.ACTIVE);
        verifyNoInteractions(scheduleRepository, scheduleAssignmentRepository);
    }
}
