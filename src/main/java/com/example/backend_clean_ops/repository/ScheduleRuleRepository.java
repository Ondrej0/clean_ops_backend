package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.ScheduleRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleRuleRepository extends JpaRepository<ScheduleRule, UUID> {
    Optional<List<ScheduleRule>> findByScheduleId(UUID scheduleId);
}
