package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateScheduleRequest;
import com.example.backend_clean_ops.dto.responses.CreateScheduleResponse;
import com.example.backend_clean_ops.dto.responses.getSchedule.GetScheduleByIdResponse;
import com.example.backend_clean_ops.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    private ScheduleController scheduleController;

    @BeforeEach
    void setUp() {
        scheduleController = new ScheduleController(scheduleService);
    }

    @Test
    void getSchedulesBySites_shouldAcceptOptionalUserId() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        var mvc = MockMvcBuilders.standaloneSetup(scheduleController).build();
        when(scheduleService.getSchedulesBySites(tenantId, null)).thenReturn(List.of());
        when(scheduleService.getSchedulesBySites(tenantId, userId)).thenReturn(List.of());

        mvc.perform(get("/api/schedules/by-site").param("tenantId", tenantId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        mvc.perform(get("/api/schedules/by-site")
                        .param("tenantId", tenantId.toString()).param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        mvc.perform(get("/api/schedules/by-site")
                        .param("tenantId", tenantId.toString()).param("userId", "invalid"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/schedules/by-site").param("userId", userId.toString()))
                .andExpect(status().isBadRequest());

        verify(scheduleService).getSchedulesBySites(tenantId, null);
        verify(scheduleService).getSchedulesBySites(tenantId, userId);
        verifyNoMoreInteractions(scheduleService);
    }

    @Test
    @DisplayName("Should delegate schedule retrieval to schedule service and return response")
    void getSchedule_shouldCallScheduleService_andReturnResponse() {
        UUID scheduleId = UUID.randomUUID();
        GetScheduleByIdResponse expectedResponse = mock(GetScheduleByIdResponse.class);

        when(scheduleService.getSchedule(scheduleId)).thenReturn(expectedResponse);

        GetScheduleByIdResponse actualResponse = scheduleController.getSchedule(scheduleId);

        assertSame(expectedResponse, actualResponse);
        verify(scheduleService).getSchedule(scheduleId);
        verifyNoMoreInteractions(scheduleService);
    }

    @Test
    @DisplayName("Should propagate exception when schedule retrieval fails")
    void getSchedule_whenScheduleServiceThrows_shouldPropagateException() {
        UUID scheduleId = UUID.randomUUID();
        RuntimeException exception = new RuntimeException("Schedule not found");

        when(scheduleService.getSchedule(scheduleId)).thenThrow(exception);

        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> scheduleController.getSchedule(scheduleId)
        );

        assertSame(exception, thrownException);
        verify(scheduleService).getSchedule(scheduleId);
        verifyNoMoreInteractions(scheduleService);
    }

    @Test
    @DisplayName("Should delegate schedule creation to schedule service and return response")
    void createAndAssignSchedule_shouldCallScheduleService_andReturnResponse() {
        CreateScheduleRequest request = mock(CreateScheduleRequest.class);
        CreateScheduleResponse expectedResponse = mock(CreateScheduleResponse.class);

        when(scheduleService.createAndAssignSchedule(request)).thenReturn(expectedResponse);

        CreateScheduleResponse actualResponse = scheduleController.createAndAssignSchedule(request);

        assertSame(expectedResponse, actualResponse);

        verify(scheduleService).createAndAssignSchedule(request);
        verifyNoMoreInteractions(scheduleService);
    }

    @Test
    @DisplayName("Should propagate exception when schedule service fails")
    void createAndAssignSchedule_whenScheduleServiceThrows_shouldPropagateException() {
        CreateScheduleRequest request = mock(CreateScheduleRequest.class);
        RuntimeException exception = new RuntimeException("Unable to create schedule");

        when(scheduleService.createAndAssignSchedule(request)).thenThrow(exception);

        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> scheduleController.createAndAssignSchedule(request)
        );

        assertSame(exception, thrownException);

        verify(scheduleService).createAndAssignSchedule(request);
        verifyNoMoreInteractions(scheduleService);
    }

    @Test
    @DisplayName("Should delegate cleaner assignment to schedule service")
    void assignCleanerToSchedule_shouldCallScheduleService() {
        UUID tenantId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID cleanerId = UUID.randomUUID();

        scheduleController.assignCleanerToSchedule(tenantId, scheduleId, cleanerId);

        verify(scheduleService).assignCleanerToSchedule(tenantId, scheduleId, cleanerId);
        verifyNoMoreInteractions(scheduleService);
    }

    @Test
    @DisplayName("Should propagate exception when cleaner assignment fails")
    void assignCleanerToSchedule_whenScheduleServiceThrows_shouldPropagateException() {
        UUID tenantId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID cleanerId = UUID.randomUUID();
        RuntimeException exception = new RuntimeException("Unable to assign cleaner");

        doThrow(exception).when(scheduleService).assignCleanerToSchedule(tenantId, scheduleId, cleanerId);

        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> scheduleController.assignCleanerToSchedule(tenantId, scheduleId, cleanerId)
        );

        assertSame(exception, thrownException);

        verify(scheduleService).assignCleanerToSchedule(tenantId, scheduleId, cleanerId);
        verifyNoMoreInteractions(scheduleService);
    }
}
