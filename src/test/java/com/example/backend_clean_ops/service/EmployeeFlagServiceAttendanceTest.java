package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.entity.EmployeeFlag;
import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.EmployeeFlagType;
import com.example.backend_clean_ops.enums.ShiftStatus;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.repository.EmployeeFlagRepository;
import com.example.backend_clean_ops.repository.ShiftRepository;
import com.example.backend_clean_ops.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeFlagServiceAttendanceTest {
    private final EmployeeFlagRepository flags = mock(EmployeeFlagRepository.class);
    private final EmployeeFlagService service = new EmployeeFlagService(flags, mock(UserRepository.class),
            mock(ShiftRepository.class), mock(CurrentEmployee.class), mock(ScheduleOverlapDetector.class),
            Clock.fixed(Instant.parse("2026-11-09T12:00:00Z"), ZoneOffset.UTC));

    @Test void exactCompletionCreatesNoException() {
        service.recordAttendanceException(shift("2026-11-09T09:00", "2026-11-09T11:00", "2026-11-09T09:00", "2026-11-09T11:00", ShiftStatus.COMPLETED));
        verifyNoInteractions(flags);
    }

    @Test void completedShortShiftCreatesOneDescriptiveOpenException() {
        Shift shift = shift("2026-11-09T09:00", "2026-11-09T11:00", "2026-11-09T09:51", "2026-11-09T09:51", ShiftStatus.COMPLETED);
        when(flags.existsByTenantIdAndEmployeeIdAndTypeAndOpenConflictKey(any(), any(), eq(EmployeeFlagType.ATTENDANCE_EXCEPTION), anyString())).thenReturn(false);

        service.recordAttendanceException(shift);

        ArgumentCaptor<EmployeeFlag> saved = ArgumentCaptor.forClass(EmployeeFlag.class);
        verify(flags).save(saved.capture());
        assertAll(
                () -> assertEquals(EmployeeFlagType.ATTENDANCE_EXCEPTION, saved.getValue().getType()),
                () -> assertEquals(shift.getId(), saved.getValue().getFirstShiftId()),
                () -> assertTrue(saved.getValue().getMessage().contains("Late by 51m")),
                () -> assertTrue(saved.getValue().getMessage().contains("Left early by 1h 9m")),
                () -> assertTrue(saved.getValue().getMessage().contains("Worked 0m of 2h"))
        );
    }

    @Test void nonCompletedAndDuplicateExceptionsAreNotSaved() {
        service.recordAttendanceException(shift("2026-11-09T09:00", "2026-11-09T11:00", "2026-11-09T09:51", "2026-11-09T09:51", ShiftStatus.NO_SHOW));
        Shift completed = shift("2026-11-09T09:00", "2026-11-09T11:00", "2026-11-09T09:51", "2026-11-09T09:51", ShiftStatus.COMPLETED);
        when(flags.existsByTenantIdAndEmployeeIdAndTypeAndOpenConflictKey(any(), any(), eq(EmployeeFlagType.ATTENDANCE_EXCEPTION), anyString())).thenReturn(true);
        service.recordAttendanceException(completed);
        verify(flags, never()).save(any());
    }

    private Shift shift(String scheduledStart, String scheduledEnd, String actualStart, String actualEnd, ShiftStatus status) {
        Tenant tenant = Tenant.builder().name("Company").build();
        User cleaner = User.builder().tenant(tenant).firstName("Alex").lastName("Smith").role(UserRole.CLEANER).active(true).build();
        Shift shift = Shift.builder().tenant(tenant).user(cleaner).scheduledStart(LocalDateTime.parse(scheduledStart))
                .scheduledEnd(LocalDateTime.parse(scheduledEnd)).actualStart(LocalDateTime.parse(actualStart))
                .actualEnd(LocalDateTime.parse(actualEnd)).status(status).build();
        org.springframework.test.util.ReflectionTestUtils.setField(tenant, "id", UUID.randomUUID());
        org.springframework.test.util.ReflectionTestUtils.setField(cleaner, "id", UUID.randomUUID());
        org.springframework.test.util.ReflectionTestUtils.setField(shift, "id", UUID.randomUUID());
        return shift;
    }
}
