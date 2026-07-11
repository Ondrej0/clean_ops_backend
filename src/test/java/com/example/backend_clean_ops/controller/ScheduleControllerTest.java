package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateScheduleRequest;
import com.example.backend_clean_ops.dto.responses.CreateScheduleResponse;
import com.example.backend_clean_ops.service.ScheduleService;
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
class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    private ScheduleController scheduleController;

    @BeforeEach
    void setUp() {
        scheduleController = new ScheduleController(scheduleService);
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
}
