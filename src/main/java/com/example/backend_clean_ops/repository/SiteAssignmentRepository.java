package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.SiteAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SiteAssignmentRepository extends JpaRepository<SiteAssignment, UUID> {
}
