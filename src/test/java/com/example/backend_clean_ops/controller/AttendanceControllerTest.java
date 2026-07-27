package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.AttendanceRequest;
import com.example.backend_clean_ops.dto.responses.AttendanceResponse;
import com.example.backend_clean_ops.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    @Mock
    private AttendanceService attendanceService;

    private AttendanceController attendanceController;

    @BeforeEach
    void setUp() {
        attendanceController = new AttendanceController(attendanceService);
    }

    @Test
    @DisplayName("Should delegate clock in to attendance service and return response")
    void clockIn_shouldCallAttendanceService_andReturnResponse() {
        AttendanceRequest request = org.mockito.Mockito.mock(AttendanceRequest.class);
        AttendanceResponse expectedResponse = org.mockito.Mockito.mock(AttendanceResponse.class);
        when(attendanceService.clockIn(request)).thenReturn(expectedResponse);

        AttendanceResponse actualResponse = attendanceController.clockIn(request);

        assertSame(expectedResponse, actualResponse);
        verify(attendanceService).clockIn(request);
        verifyNoMoreInteractions(attendanceService);
    }

    @Test
    @DisplayName("Should propagate exception when clock in fails")
    void clockIn_whenAttendanceServiceThrows_shouldPropagateException() {
        AttendanceRequest request = org.mockito.Mockito.mock(AttendanceRequest.class);
        RuntimeException expectedException = new RuntimeException("Unable to clock in");
        when(attendanceService.clockIn(request)).thenThrow(expectedException);

        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> attendanceController.clockIn(request)
        );

        assertSame(expectedException, actualException);
        verify(attendanceService).clockIn(request);
        verifyNoMoreInteractions(attendanceService);
    }

    @Test
    @DisplayName("Should delegate clock out to attendance service and return response")
    void clockOut_shouldCallAttendanceService_andReturnResponse() {
        AttendanceRequest request = org.mockito.Mockito.mock(AttendanceRequest.class);
        AttendanceResponse expectedResponse = org.mockito.Mockito.mock(AttendanceResponse.class);
        when(attendanceService.clockOut(request)).thenReturn(expectedResponse);

        AttendanceResponse actualResponse = attendanceController.clockOut(request);

        assertSame(expectedResponse, actualResponse);
        verify(attendanceService).clockOut(request);
        verifyNoMoreInteractions(attendanceService);
    }

    @Test
    @DisplayName("Should propagate exception when clock out fails")
    void clockOut_whenAttendanceServiceThrows_shouldPropagateException() {
        AttendanceRequest request = org.mockito.Mockito.mock(AttendanceRequest.class);
        RuntimeException expectedException = new RuntimeException("Unable to clock out");
        when(attendanceService.clockOut(request)).thenThrow(expectedException);

        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> attendanceController.clockOut(request)
        );

        assertSame(expectedException, actualException);
        verify(attendanceService).clockOut(request);
        verifyNoMoreInteractions(attendanceService);
    }
}
