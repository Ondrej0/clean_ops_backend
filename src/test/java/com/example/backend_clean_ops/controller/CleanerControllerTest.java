package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateUserRequest;
import com.example.backend_clean_ops.dto.responses.CreateUserResponse;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.service.UserService;
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
class CleanerControllerTest {

    @Mock
    private UserService userService;

    private CleanerController cleanerController;

    @BeforeEach
    void setUp() {
        cleanerController = new CleanerController(userService);
    }

    @Test
    @DisplayName("Should create cleaner using CLEANER role and return service response")
    void createCleaner_shouldCallUserServiceWithCleanerRole_andReturnResponse() {
        // Arrange
        CreateUserRequest request = mock(CreateUserRequest.class);
        CreateUserResponse expectedResponse = mock(CreateUserResponse.class);

        when(userService.createUser(request, UserRole.CLEANER))
                .thenReturn(expectedResponse);

        // Act
        CreateUserResponse actualResponse =
                cleanerController.createCleaner(request);

        // Assert
        assertSame(expectedResponse, actualResponse);

        verify(userService).createUser(request, UserRole.CLEANER);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("Should propagate exception when user service fails")
    void createCleaner_whenUserServiceThrows_shouldPropagateException() {
        // Arrange
        CreateUserRequest request = mock(CreateUserRequest.class);

        RuntimeException exception =
                new RuntimeException("Unable to create cleaner");

        when(userService.createUser(request, UserRole.CLEANER))
                .thenThrow(exception);

        // Act
        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> cleanerController.createCleaner(request)
        );

        // Assert
        assertSame(exception, thrownException);

        verify(userService).createUser(request, UserRole.CLEANER);
        verifyNoMoreInteractions(userService);
    }
}