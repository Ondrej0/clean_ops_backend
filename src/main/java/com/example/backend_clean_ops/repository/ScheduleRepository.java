package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    List<Schedule> findAllBySiteIdAndActiveTrue(UUID siteId);

    @Query("""
            select schedule
            from Schedule schedule
            where schedule.site.id = :siteId
              and schedule.active = true
              and (:userId is null or exists (
                  select assignment.id
                  from ScheduleAssignment assignment
                  where assignment.schedule = schedule
                    and assignment.user.id = :userId
              ))
            """)
    List<Schedule> findActiveBySiteIdAndUserId(@Param("siteId") UUID siteId,
                                             @Param("userId") UUID userId);

    java.util.Optional<Schedule> findByIdAndActiveTrue(UUID id);
}
