package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.enums.SiteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {
    List<Site> findAllByTenantIdAndStatus(UUID tenantId, SiteStatus status);

    java.util.Optional<Site> findByIdAndStatus(UUID id, SiteStatus status);
}
