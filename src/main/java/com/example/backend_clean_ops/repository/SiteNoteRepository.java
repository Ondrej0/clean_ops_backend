package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.SiteNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SiteNoteRepository extends JpaRepository<SiteNote, UUID> {
}
