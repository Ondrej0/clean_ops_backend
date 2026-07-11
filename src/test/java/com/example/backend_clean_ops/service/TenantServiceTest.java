package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateTenantRequest;
import com.example.backend_clean_ops.dto.responses.CreateTenantResponse;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    private TenantService tenantService;

    @BeforeEach
    void setUp() {
        tenantService = new TenantService(tenantRepository);
    }

    @Test
    @DisplayName("Should map tenant request, save the tenant, and return the created response")
    void createTenant_shouldSaveTenantAndReturnResponse() {
        UUID tenantId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 11, 12, 0);
        Tenant savedTenant = mock(Tenant.class);
        CreateTenantRequest request = new CreateTenantRequest(
                "Clean Ops Ltd",
                "Alice Smith",
                "alice@example.com",
                "100 Main Street",
                "Manchester",
                "M1 1AA"
        );

        when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);
        when(savedTenant.getId()).thenReturn(tenantId);
        when(savedTenant.getName()).thenReturn("Clean Ops Ltd");
        when(savedTenant.getCreatedAt()).thenReturn(createdAt);

        CreateTenantResponse response = tenantService.createTenant(request);

        assertAll(
                () -> assertEquals(tenantId, response.tenantID()),
                () -> assertEquals("Clean Ops Ltd", response.name()),
                () -> assertEquals(createdAt, response.createdAt())
        );

        ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(tenantCaptor.capture());

        Tenant savedTenantEntity = tenantCaptor.getValue();
        assertAll(
                () -> assertEquals("Clean Ops Ltd", savedTenantEntity.getName()),
                () -> assertEquals("Alice Smith", savedTenantEntity.getContactName()),
                () -> assertEquals("alice@example.com", savedTenantEntity.getContactEmail()),
                () -> assertEquals("100 Main Street", savedTenantEntity.getAddressLine1()),
                () -> assertEquals("Manchester", savedTenantEntity.getCity()),
                () -> assertEquals("M1 1AA", savedTenantEntity.getPostcode())
        );

        verifyNoMoreInteractions(tenantRepository);
    }

    @Test
    @DisplayName("Should propagate repository exceptions when tenant save fails")
    void createTenant_whenRepositoryThrows_shouldPropagateException() {
        CreateTenantRequest request = new CreateTenantRequest(
                "Clean Ops Ltd",
                "Alice Smith",
                "alice@example.com",
                "100 Main Street",
                "Manchester",
                "M1 1AA"
        );
        RuntimeException exception = new RuntimeException("Database unavailable");

        when(tenantRepository.save(any(Tenant.class))).thenThrow(exception);

        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> tenantService.createTenant(request)
        );

        assertSame(exception, thrownException);

        verify(tenantRepository).save(any(Tenant.class));
        verifyNoMoreInteractions(tenantRepository);
    }
}
