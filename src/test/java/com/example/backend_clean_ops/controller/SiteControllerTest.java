package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateSiteRequest;
import com.example.backend_clean_ops.dto.responses.CreateSiteResponse;
import com.example.backend_clean_ops.service.SiteService;
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
class SiteControllerTest {

    @Mock
    private SiteService siteService;

    private SiteController siteController;

    @BeforeEach
    void setUp() {
        siteController = new SiteController(siteService);
    }

    @Test
    @DisplayName("Should delegate site creation to site service and return response")
    void createSite_shouldCallSiteService_andReturnResponse() {
        CreateSiteRequest request = mock(CreateSiteRequest.class);
        CreateSiteResponse expectedResponse = mock(CreateSiteResponse.class);

        when(siteService.createSite(request)).thenReturn(expectedResponse);

        CreateSiteResponse actualResponse = siteController.createSite(request);

        assertSame(expectedResponse, actualResponse);

        verify(siteService).createSite(request);
        verifyNoMoreInteractions(siteService);
    }

    @Test
    @DisplayName("Should propagate exception when site service fails")
    void createSite_whenSiteServiceThrows_shouldPropagateException() {
        CreateSiteRequest request = mock(CreateSiteRequest.class);
        RuntimeException exception = new RuntimeException("Unable to create site");

        when(siteService.createSite(request)).thenThrow(exception);

        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> siteController.createSite(request)
        );

        assertSame(exception, thrownException);

        verify(siteService).createSite(request);
        verifyNoMoreInteractions(siteService);
    }
}
