package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.enums.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    @Query("""
            select s from Shift s join fetch s.site
            where s.tenant.id = :tenantId and s.user.id = :userId
              and s.scheduledStart < :until and s.scheduledEnd > :from
            order by s.scheduledStart, s.id
            """)
    List<Shift> findInWindow(@Param("tenantId") UUID tenantId, @Param("userId") UUID userId,
                             @Param("from") LocalDateTime from, @Param("until") LocalDateTime until);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Shift s where s.id = :id")
    Optional<Shift> findForAttendance(@Param("id") UUID id);

    long deleteByScheduleIdAndShiftDateGreaterThanEqualAndStatus(
            UUID scheduleId,
            LocalDate shiftDate,
            ShiftStatus status
    );

}
