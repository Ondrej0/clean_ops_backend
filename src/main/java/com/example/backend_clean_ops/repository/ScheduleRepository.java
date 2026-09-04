package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    List<Schedule> findAllBySiteIdAndActiveTrue(UUID siteId);

    java.util.Optional<Schedule> findByIdAndActiveTrue(UUID id);
}
