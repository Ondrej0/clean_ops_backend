package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {
}
