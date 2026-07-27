package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.AttendanceRequest;
import com.example.backend_clean_ops.dto.responses.AttendanceResponse;
import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.ShiftStatus;
import com.example.backend_clean_ops.repository.ShiftRepository;
import com.example.backend_clean_ops.repository.SiteRepository;
import com.example.backend_clean_ops.repository.TenantRepository;
import com.example.backend_clean_ops.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private ShiftRepository shiftRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SiteRepository siteRepository;

    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(
                shiftRepository,
                tenantRepository,
                userRepository,
                siteRepository
        );
    }

    @Test
    @DisplayName("Should set actual start and mark shift in progress when clocking in")
    void clockIn_whenRequestMatchesShift_shouldUpdateAndSaveShift() {
        TestData data = matchingTestData();

        AttendanceResponse response = attendanceService.clockIn(data.request());

        assertAll(
                () -> assertEquals(data.clockedAt(), data.shift().getActualStart()),
                () -> assertNull(data.shift().getActualEnd()),
                () -> assertEquals(ShiftStatus.IN_PROGRESS, data.shift().getStatus()),
                () -> assertEquals(ShiftStatus.IN_PROGRESS, response.shiftStatus())
        );
        verifyLookupsAndSave(data);
    }

    @Test
    @DisplayName("Should set actual end and mark shift completed when clocking out")
    void clockOut_whenRequestMatchesShift_shouldUpdateAndSaveShift() {
        TestData data = matchingTestData();
        data.shift().setStatus(ShiftStatus.IN_PROGRESS);

        AttendanceResponse response = attendanceService.clockOut(data.request());

        assertAll(
                () -> assertNull(data.shift().getActualStart()),
                () -> assertEquals(data.clockedAt(), data.shift().getActualEnd()),
                () -> assertEquals(ShiftStatus.COMPLETED, data.shift().getStatus()),
                () -> assertEquals(ShiftStatus.COMPLETED, response.shiftStatus())
        );
        verifyLookupsAndSave(data);
    }

    @Test
    @DisplayName("Should reject clock in when shift cannot be found")
    void clockIn_whenShiftMissing_shouldThrowException() {
        AttendanceRequest request = request();
        when(shiftRepository.findById(request.shiftID())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> attendanceService.clockIn(request)
        );

        assertEquals("Shift ID not found.", exception.getMessage());
        verify(shiftRepository).findById(request.shiftID());
        verifyNoInteractions(tenantRepository, userRepository, siteRepository);
        verifyNoMoreInteractions(shiftRepository);
    }

    @Test
    @DisplayName("Should reject clock out when user cannot be found")
    void clockOut_whenUserMissing_shouldThrowException() {
        AttendanceRequest request = request();
        Shift shift = mock(Shift.class);
        Tenant tenant = mock(Tenant.class);
        when(shiftRepository.findById(request.shiftID())).thenReturn(Optional.of(shift));
        when(tenantRepository.findById(request.tenantID())).thenReturn(Optional.of(tenant));
        when(userRepository.findById(request.userID())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> attendanceService.clockOut(request)
        );

        assertEquals("User ID not found.", exception.getMessage());
        verify(shiftRepository).findById(request.shiftID());
        verify(tenantRepository).findById(request.tenantID());
        verify(userRepository).findById(request.userID());
        verifyNoInteractions(siteRepository);
        verify(shiftRepository, never()).save(shift);
        verifyNoMoreInteractions(shiftRepository, tenantRepository, userRepository);
    }

    @Test
    @DisplayName("Should reject clock in when tenant does not match the shift")
    void clockIn_whenTenantDoesNotMatch_shouldThrowException() {
        TestData data = matchingTestData();
        when(data.shiftTenant().getId()).thenReturn(UUID.randomUUID());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> attendanceService.clockIn(data.request())
        );

        assertEquals("Tenant does not match", exception.getMessage());
        verify(shiftRepository, never()).save(data.shift());
    }

    @Test
    @DisplayName("Should reject clock out when user does not match the shift")
    void clockOut_whenUserDoesNotMatch_shouldThrowException() {
        TestData data = matchingTestData();
        when(data.shiftUser().getId()).thenReturn(UUID.randomUUID());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> attendanceService.clockOut(data.request())
        );

        assertEquals("User does not match", exception.getMessage());
        verify(shiftRepository, never()).save(data.shift());
    }

    @Test
    @DisplayName("Should reject clock out when site does not match the shift")
    void clockOut_whenSiteDoesNotMatch_shouldThrowException() {
        TestData data = matchingTestData();
        when(data.shiftSite().getId()).thenReturn(UUID.randomUUID());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> attendanceService.clockOut(data.request())
        );

        assertEquals("Site does not match", exception.getMessage());
        verify(shiftRepository, never()).save(data.shift());
    }

    private TestData matchingTestData() {
        AttendanceRequest request = request();
        Shift shift = new Shift();
        Tenant requestedTenant = mock(Tenant.class);
        Tenant shiftTenant = mock(Tenant.class);
        User requestedUser = mock(User.class);
        User shiftUser = mock(User.class);
        Site requestedSite = mock(Site.class);
        Site shiftSite = mock(Site.class);

        shift.setTenant(shiftTenant);
        shift.setUser(shiftUser);
        shift.setSite(shiftSite);

        when(shiftRepository.findById(request.shiftID())).thenReturn(Optional.of(shift));
        when(tenantRepository.findById(request.tenantID())).thenReturn(Optional.of(requestedTenant));
        when(userRepository.findById(request.userID())).thenReturn(Optional.of(requestedUser));
        when(siteRepository.findById(request.siteID())).thenReturn(Optional.of(requestedSite));
        lenient().when(requestedTenant.getId()).thenReturn(request.tenantID());
        lenient().when(shiftTenant.getId()).thenReturn(request.tenantID());
        lenient().when(requestedUser.getId()).thenReturn(request.userID());
        lenient().when(shiftUser.getId()).thenReturn(request.userID());
        lenient().when(requestedSite.getId()).thenReturn(request.siteID());
        lenient().when(shiftSite.getId()).thenReturn(request.siteID());

        return new TestData(request, shift, shiftTenant, shiftUser, shiftSite, request.clockedIn());
    }

    private AttendanceRequest request() {
        return new AttendanceRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.of(2026, 7, 27, 9, 30)
        );
    }

    private void verifyLookupsAndSave(TestData data) {
        verify(shiftRepository).findById(data.request().shiftID());
        verify(tenantRepository).findById(data.request().tenantID());
        verify(userRepository).findById(data.request().userID());
        verify(siteRepository).findById(data.request().siteID());
        verify(shiftRepository).save(data.shift());
    }

    private record TestData(
            AttendanceRequest request,
            Shift shift,
            Tenant shiftTenant,
            User shiftUser,
            Site shiftSite,
            LocalDateTime clockedAt
    ) {
    }
}
