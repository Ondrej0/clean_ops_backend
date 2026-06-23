package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {
}
