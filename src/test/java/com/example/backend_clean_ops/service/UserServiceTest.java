package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateUserRequest;
import com.example.backend_clean_ops.dto.responses.CleanerResponse;
import com.example.backend_clean_ops.dto.responses.CreateUserResponse;
import com.example.backend_clean_ops.dto.responses.GetCleanersResponse;
import com.example.backend_clean_ops.dto.responses.GetCleanerDataResponse;
import com.example.backend_clean_ops.dto.responses.AssignedScheduleResponse;
import com.example.backend_clean_ops.dto.responses.AssignedSiteResponse;
import com.example.backend_clean_ops.entity.Schedule;
import com.example.backend_clean_ops.entity.ScheduleAssignment;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.repository.TenantRepository;
import com.example.backend_clean_ops.repository.ScheduleAssignmentRepository;
import com.example.backend_clean_ops.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ScheduleAssignmentRepository scheduleAssignmentRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, tenantRepository, scheduleAssignmentRepository);
    }

    @Test
    @DisplayName("Should map user request, save the user, and return the created response")
    void createUser_whenTenantExists_shouldSaveUserAndReturnResponse() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 11, 10, 30);
        Tenant tenant = mock(Tenant.class);
        User savedUser = mock(User.class);
        CreateUserRequest request = new CreateUserRequest(
                tenantId,
                "Jane",
                "Doe",
                "hashed-password",
                "jane@example.com",
                19.50f
        );

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(savedUser.getId()).thenReturn(userId);
        when(savedUser.getFirstName()).thenReturn("Jane");
        when(savedUser.getLastName()).thenReturn("Doe");
        when(savedUser.getCreatedAt()).thenReturn(createdAt);

        CreateUserResponse response = userService.createUser(request, UserRole.CLEANER);

        assertAll(
                () -> assertEquals(userId, response.userId()),
                () -> assertEquals("Jane", response.firstName()),
                () -> assertEquals("Doe", response.lastName()),
                () -> assertEquals(createdAt, response.createdAt())
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(tenantRepository).findById(tenantId);
        verify(userRepository).save(userCaptor.capture());

        User savedUserEntity = userCaptor.getValue();
        assertAll(
                () -> assertSame(tenant, savedUserEntity.getTenant()),
                () -> assertEquals("Jane", savedUserEntity.getFirstName()),
                () -> assertEquals("Doe", savedUserEntity.getLastName()),
                () -> assertEquals("hashed-password", savedUserEntity.getPasswordHash()),
                () -> assertEquals("jane@example.com", savedUserEntity.getEmail()),
                () -> assertEquals(UserRole.CLEANER, savedUserEntity.getRole())
        );

        verifyNoMoreInteractions(userRepository, tenantRepository);
    }

    @Test
    @DisplayName("Should throw when tenant cannot be found")
    void createUser_whenTenantMissing_shouldThrowException() {
        UUID tenantId = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest(
                tenantId,
                "Jane",
                "Doe",
                "hashed-password",
                "jane@example.com",
                19.50f
        );

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.createUser(request, UserRole.CLEANER)
        );

        assertEquals("Tenant not found", exception.getMessage());

        verify(tenantRepository).findById(tenantId);
        verifyNoInteractions(userRepository);
        verifyNoMoreInteractions(tenantRepository);
    }

    @Test
    @DisplayName("Should return cleaners mapped into cleaner responses for tenant")
    void getCleaners_whenCleanersExist_shouldReturnMappedCleanerResponses() {
        UUID tenantId = UUID.randomUUID();
        UUID firstCleanerId = UUID.randomUUID();
        UUID secondCleanerId = UUID.randomUUID();
        User firstCleaner = mock(User.class);
        User secondCleaner = mock(User.class);

        when(userRepository.findAllByTenantIdAndRoleAndActiveTrue(tenantId, UserRole.CLEANER))
                .thenReturn(List.of(firstCleaner, secondCleaner));
        when(firstCleaner.getId()).thenReturn(firstCleanerId);
        when(firstCleaner.getFirstName()).thenReturn("Jane");
        when(firstCleaner.getLastName()).thenReturn("Doe");
        when(firstCleaner.getEmail()).thenReturn("jane@example.com");
        when(firstCleaner.getPhone()).thenReturn("07123456789");
        when(secondCleaner.getId()).thenReturn(secondCleanerId);
        when(secondCleaner.getFirstName()).thenReturn("John");
        when(secondCleaner.getLastName()).thenReturn("Smith");
        when(secondCleaner.getEmail()).thenReturn("john@example.com");
        when(secondCleaner.getPhone()).thenReturn("07987654321");

        GetCleanersResponse response = userService.getCleaners(tenantId);

        assertEquals(List.of(
                new CleanerResponse(firstCleanerId, "Jane", "Doe", "jane@example.com", "07123456789"),
                new CleanerResponse(secondCleanerId, "John", "Smith", "john@example.com", "07987654321")
        ), response.cleaners());

        verify(userRepository).findAllByTenantIdAndRoleAndActiveTrue(tenantId, UserRole.CLEANER);
        verifyNoInteractions(tenantRepository);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should return an empty list when tenant has no cleaners")
    void getCleaners_whenNoCleanersExist_shouldReturnEmptyResponse() {
        UUID tenantId = UUID.randomUUID();
        when(userRepository.findAllByTenantIdAndRoleAndActiveTrue(tenantId, UserRole.CLEANER)).thenReturn(List.of());

        GetCleanersResponse response = userService.getCleaners(tenantId);

        assertTrue(response.cleaners().isEmpty());
        verify(userRepository).findAllByTenantIdAndRoleAndActiveTrue(tenantId, UserRole.CLEANER);
        verifyNoInteractions(tenantRepository);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should return cleaner data with schedules and distinct sites derived from schedules")
    void getCleanerData_whenCleanerExists_shouldMapProfileSchedulesAndSites() {
        UUID cleanerId = UUID.randomUUID();
        UUID firstScheduleId = UUID.randomUUID();
        UUID secondScheduleId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 11, 10, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 12, 11, 45);
        User cleaner = mock(User.class);
        ScheduleAssignment firstAssignment = mock(ScheduleAssignment.class);
        ScheduleAssignment secondAssignment = mock(ScheduleAssignment.class);
        Schedule firstSchedule = mock(Schedule.class);
        Schedule secondSchedule = mock(Schedule.class);
        Site site = mock(Site.class);

        when(userRepository.findById(cleanerId)).thenReturn(Optional.of(cleaner));
        when(cleaner.getRole()).thenReturn(UserRole.CLEANER);
        when(scheduleAssignmentRepository.findByUserIdWithScheduleAndSite(cleanerId))
                .thenReturn(List.of(firstAssignment, secondAssignment));
        when(firstAssignment.getSchedule()).thenReturn(firstSchedule);
        when(secondAssignment.getSchedule()).thenReturn(secondSchedule);
        when(firstSchedule.getId()).thenReturn(firstScheduleId);
        when(firstSchedule.getName()).thenReturn("Morning clean");
        when(firstSchedule.getSite()).thenReturn(site);
        when(secondSchedule.getId()).thenReturn(secondScheduleId);
        when(secondSchedule.getName()).thenReturn("Evening clean");
        when(secondSchedule.getSite()).thenReturn(site);
        when(site.getId()).thenReturn(siteId);
        when(site.getName()).thenReturn("Central Office");
        when(site.getAddressLine1()).thenReturn("1 High Street");
        when(site.getPostcode()).thenReturn("SW1A 1AA");
        when(cleaner.getId()).thenReturn(cleanerId);
        when(cleaner.getFirstName()).thenReturn("Jane");
        when(cleaner.getLastName()).thenReturn("Doe");
        when(cleaner.getEmail()).thenReturn("jane@example.com");
        when(cleaner.getPhone()).thenReturn("07123456789");
        when(cleaner.getPayRate()).thenReturn(new BigDecimal("19.50"));
        when(cleaner.isActive()).thenReturn(true);
        when(cleaner.getCreatedAt()).thenReturn(createdAt);
        when(cleaner.getUpdatedAt()).thenReturn(updatedAt);

        GetCleanerDataResponse response = userService.getCleanerData(cleanerId);

        assertAll(
                () -> assertEquals(cleanerId, response.id()),
                () -> assertEquals("Jane", response.firstName()),
                () -> assertEquals(new BigDecimal("19.50"), response.payRate()),
                () -> assertEquals(List.of(
                        new AssignedScheduleResponse(firstScheduleId, "Morning clean"),
                        new AssignedScheduleResponse(secondScheduleId, "Evening clean")
                ), response.assignedSchedules()),
                () -> assertEquals(List.of(
                        new AssignedSiteResponse(siteId, "Central Office", "1 High Street", "SW1A 1AA")
                ), response.assignedSites())
        );

        verify(userRepository).findById(cleanerId);
        verify(scheduleAssignmentRepository).findByUserIdWithScheduleAndSite(cleanerId);
        verifyNoMoreInteractions(userRepository, tenantRepository, scheduleAssignmentRepository);
    }

    @Test
    @DisplayName("Should reject a user who is not a cleaner")
    void getCleanerData_whenUserIsNotCleaner_shouldThrowException() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getRole()).thenReturn(UserRole.MANAGER);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getCleanerData(userId));

        assertEquals("Cleaner not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(scheduleAssignmentRepository, tenantRepository);
        verifyNoMoreInteractions(userRepository);
    }
}
