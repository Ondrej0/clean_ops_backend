package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateSiteRequest;
import com.example.backend_clean_ops.dto.request.EditSiteRequest;
import com.example.backend_clean_ops.dto.responses.CreateSiteResponse;
import com.example.backend_clean_ops.dto.responses.GetSitesResponse;
import com.example.backend_clean_ops.dto.responses.getSite.GetSiteByIdResponse;
import com.example.backend_clean_ops.enums.SiteStatus;
import com.example.backend_clean_ops.service.SiteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SiteControllerTest {
    private static com.example.backend_clean_ops.service.ResourceAccess mockAccess() {
        return org.mockito.Mockito.mock(com.example.backend_clean_ops.service.ResourceAccess.class, invocation -> {
            if (invocation.getMethod().getReturnType() == java.util.UUID.class) return invocation.getArgument(0);
            return null;
        });
    }


    @Mock
    private SiteService siteService;

    private SiteController siteController;

    @BeforeEach
    void setUp() {
        siteController = new SiteController(mockAccess(), siteService);
    }

    @Test
    @DisplayName("Should delegate site creation to site service and return response")
    void createSite_shouldCallSiteService_andReturnResponse() {
        CreateSiteRequest request = new CreateSiteRequest(UUID.randomUUID(), "Office", "1 Road", "London", "SW1A 1AA");
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
        CreateSiteRequest request = new CreateSiteRequest(UUID.randomUUID(), "Office", "1 Road", "London", "SW1A 1AA");
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

    @ParameterizedTest
    @MethodSource("siteFilters")
    @DisplayName("Should pass optional site filters to the service and return its response")
    void getSites_shouldPassFiltersToSiteService_andReturnResponse(UUID userId, SiteStatus status) {
        UUID tenantId = UUID.randomUUID();
        GetSitesResponse expectedResponse = mock(GetSitesResponse.class);

        when(siteService.getSites(tenantId, userId, status)).thenReturn(expectedResponse);

        GetSitesResponse actualResponse = siteController.getSites(tenantId, userId, status);

        assertSame(expectedResponse, actualResponse);

        verify(siteService).getSites(tenantId, userId, status);
        verifyNoMoreInteractions(siteService);
    }

    @Test
    @DisplayName("Should propagate exception when site retrieval fails")
    void getSite_whenSiteServiceThrows_shouldPropagateException() {
        UUID tenantId = UUID.randomUUID();
        RuntimeException exception = new RuntimeException("Unable to retrieve sites");

        when(siteService.getSites(tenantId, null, null)).thenThrow(exception);

        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> siteController.getSites(tenantId, null, null)
        );

        assertSame(exception, thrownException);

        verify(siteService).getSites(tenantId, null, null);
        verifyNoMoreInteractions(siteService);
    }

    @Test
    @DisplayName("Should delegate site-detail retrieval to the site service")
    void getSiteById_shouldReturnServiceResponse() {
        UUID siteId = UUID.randomUUID();
        GetSiteByIdResponse expectedResponse = mock(GetSiteByIdResponse.class);
        when(siteService.getSiteById(siteId)).thenReturn(expectedResponse);

        GetSiteByIdResponse actualResponse = siteController.getSiteById(siteId);

        assertSame(expectedResponse, actualResponse);
        verify(siteService).getSiteById(siteId);
        verifyNoMoreInteractions(siteService);
    }

    @Test
    @DisplayName("Should delegate a site edit using the identifier from the URL")
    void editSiteById_shouldPassPathSiteIdAndRequestToService() {
        UUID siteId = UUID.randomUUID();
        EditSiteRequest request = new EditSiteRequest(UUID.randomUUID(), "Office", "1 Road", null, "London", "SW1A 1AA", null, null, null, null);

        siteController.editSiteById(siteId, request);

        verify(siteService).editSiteById(siteId, request);
        verifyNoMoreInteractions(siteService);
    }

    private static Stream<Arguments> siteFilters() {
        UUID userId = UUID.randomUUID();
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, SiteStatus.ACTIVE),
                Arguments.of(null, SiteStatus.INACTIVE),
                Arguments.of(userId, null),
                Arguments.of(userId, SiteStatus.ACTIVE),
                Arguments.of(userId, SiteStatus.INACTIVE)
        );
    }
}
