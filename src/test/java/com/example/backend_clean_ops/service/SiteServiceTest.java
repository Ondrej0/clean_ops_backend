package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateSiteRequest;
import com.example.backend_clean_ops.dto.responses.CreateSiteResponse;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Tenant;
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

    private SiteService siteService;

    @BeforeEach
    void setUp() {
        siteService = new SiteService(siteRepository, tenantRepository);
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
}
