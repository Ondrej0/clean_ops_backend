package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateUserRequest;
import com.example.backend_clean_ops.dto.request.EditEmployeeRequest;
import com.example.backend_clean_ops.dto.responses.CreateUserResponse;
import com.example.backend_clean_ops.dto.responses.GetCleanersResponse;
import com.example.backend_clean_ops.dto.responses.GetCleanerDataResponse;
import com.example.backend_clean_ops.service.EmployeeAccountService;
import com.example.backend_clean_ops.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CleanerControllerTest {

    @Mock
    private UserService userService;

    @Mock private EmployeeAccountService accounts;
    private CleanerController cleanerController;

    @BeforeEach
    void setUp() {
        cleanerController = new CleanerController(userService, accounts);
    }

    @Test
    @DisplayName("Should create cleaner using CLEANER role and return service response")
    void createCleaner_shouldCallUserServiceWithCleanerRole_andReturnResponse() {
        // Arrange
        CreateUserRequest request = mock(CreateUserRequest.class);
        CreateUserResponse expectedResponse = mock(CreateUserResponse.class);

        when(accounts.createCleaner(request))
                .thenReturn(expectedResponse);

        // Act
        CreateUserResponse actualResponse =
                cleanerController.createCleaner(request);

        // Assert
        assertSame(expectedResponse, actualResponse);

        verify(accounts).createCleaner(request);
        verifyNoMoreInteractions(userService, accounts);
    }

    @Test
    @DisplayName("Should propagate exception when user service fails")
    void createCleaner_whenUserServiceThrows_shouldPropagateException() {
        // Arrange
        CreateUserRequest request = mock(CreateUserRequest.class);

        RuntimeException exception =
                new RuntimeException("Unable to create cleaner");

        when(accounts.createCleaner(request))
                .thenThrow(exception);

        // Act
        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> cleanerController.createCleaner(request)
        );

        // Assert
        assertSame(exception, thrownException);

        verify(accounts).createCleaner(request);
        verifyNoMoreInteractions(userService, accounts);
    }

    @Test
    @DisplayName("Should delegate listing cleaners to the user service")
    void getCleaners_shouldReturnServiceResponse() {
        UUID tenantId = UUID.randomUUID();
        GetCleanersResponse expectedResponse = mock(GetCleanersResponse.class);
        when(userService.getCleaners(tenantId)).thenReturn(expectedResponse);

        GetCleanersResponse actualResponse = cleanerController.getCleaners(tenantId);

        assertSame(expectedResponse, actualResponse);
        verify(userService).getCleaners(tenantId);
        verifyNoMoreInteractions(userService, accounts);
    }

    @Test
    @DisplayName("Should delegate cleaner detail retrieval to the user service")
    void getCleanerData_shouldReturnServiceResponse() {
        UUID cleanerId = UUID.randomUUID();
        GetCleanerDataResponse expectedResponse = mock(GetCleanerDataResponse.class);
        when(userService.getCleanerData(cleanerId)).thenReturn(expectedResponse);

        GetCleanerDataResponse actualResponse = cleanerController.getCleanerData(cleanerId);

        assertSame(expectedResponse, actualResponse);
        verify(userService).getCleanerData(cleanerId);
        verifyNoMoreInteractions(userService, accounts);
    }

    @Test
    @DisplayName("Should delegate an employee edit using the identifier from the URL")
    void editEmployeeById_shouldPassPathEmployeeIdAndRequestToService() {
        UUID employeeId = UUID.randomUUID();
        EditEmployeeRequest request = mock(EditEmployeeRequest.class);

        cleanerController.editEmployeeById(employeeId, request);

        verify(accounts).edit(employeeId, request);
        verifyNoMoreInteractions(userService, accounts);
    }
}
