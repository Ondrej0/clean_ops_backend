package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.enums.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    long deleteByScheduleIdAndShiftDateGreaterThanEqualAndStatus(
            UUID scheduleId,
            LocalDate shiftDate,
            ShiftStatus status
    );
}
