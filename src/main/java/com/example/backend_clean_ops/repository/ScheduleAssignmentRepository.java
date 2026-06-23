package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.ScheduleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScheduleAssignmentRepository extends JpaRepository<ScheduleAssignment, UUID> {
}
