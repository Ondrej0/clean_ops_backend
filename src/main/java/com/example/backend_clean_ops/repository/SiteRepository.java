package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.enums.SiteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {
    @Query("""
            select site
            from Site site
            where site.tenant.id = :tenantId
              and (:status is null or site.status = :status)
              and (:userId is null or exists (
                  select assignment.id
                  from ScheduleAssignment assignment
                  where assignment.schedule.site = site
                    and assignment.user.id = :userId
              ))
            """)
    List<Site> findByFilters(@Param("tenantId") UUID tenantId,
                             @Param("userId") UUID userId,
                             @Param("status") SiteStatus status);

    List<Site> findAllByTenantIdAndStatus(UUID tenantId, SiteStatus status);

    java.util.Optional<Site> findByIdAndStatus(UUID id, SiteStatus status);
}
