package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChecklistRepository extends JpaRepository<Checklist, UUID> {
}
