package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.ScheduleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ScheduleAssignmentRepository extends JpaRepository<ScheduleAssignment, UUID> {
    boolean existsByScheduleIdAndUserId(UUID scheduleId, UUID userId);
    List<ScheduleAssignment> findByScheduleId(UUID scheduleId);

    @Query("""
            select assignment
            from ScheduleAssignment assignment
            join fetch assignment.schedule schedule
            join fetch assignment.user user
            where schedule.site.id = :siteId
              and schedule.active = true
              and user.active = true
            """)
    List<ScheduleAssignment> findBySiteIdWithActiveScheduleAndActiveUser(@Param("siteId") UUID siteId);

    @Query("""
            select assignment
            from ScheduleAssignment assignment
            join fetch assignment.schedule schedule
            join fetch schedule.site
            where assignment.user.id = :userId
            """)
    List<ScheduleAssignment> findByUserIdWithScheduleAndSite(@Param("userId") UUID userId);
}
