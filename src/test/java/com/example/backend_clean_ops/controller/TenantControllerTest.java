package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateTenantRequest;
import com.example.backend_clean_ops.dto.responses.CreateTenantResponse;
import com.example.backend_clean_ops.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    private TenantController tenantController;

    @BeforeEach
    void setUp() {
        tenantController = new TenantController(tenantService);
    }

    @Test
    @DisplayName("Should delegate tenant creation to tenant service and return response")
    void createTenant_shouldCallTenantService_andReturnResponse() {
        CreateTenantRequest request = mock(CreateTenantRequest.class);
        CreateTenantResponse expectedResponse = mock(CreateTenantResponse.class);

        when(tenantService.createTenant(request)).thenReturn(expectedResponse);

        CreateTenantResponse actualResponse = tenantController.createTenant(request);

        assertSame(expectedResponse, actualResponse);

        verify(tenantService).createTenant(request);
        verifyNoMoreInteractions(tenantService);
    }

    @Test
    @DisplayName("Should propagate exception when tenant service fails")
    void createTenant_whenTenantServiceThrows_shouldPropagateException() {
        CreateTenantRequest request = mock(CreateTenantRequest.class);
        RuntimeException exception = new RuntimeException("Unable to create tenant");

        when(tenantService.createTenant(request)).thenThrow(exception);

        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> tenantController.createTenant(request)
        );

        assertSame(exception, thrownException);

        verify(tenantService).createTenant(request);
        verifyNoMoreInteractions(tenantService);
    }
}
