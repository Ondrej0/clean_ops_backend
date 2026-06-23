package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, UUID> {
}
