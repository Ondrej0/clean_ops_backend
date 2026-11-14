package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.EmployeeFlag;
import com.example.backend_clean_ops.enums.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface EmployeeFlagRepository extends JpaRepository<EmployeeFlag, UUID> {
    boolean existsByTenantIdAndEmployeeIdAndTypeAndOpenConflictKey(UUID tenantId, UUID employeeId,
            EmployeeFlagType type, String openConflictKey);

    @Query("""
            select f from EmployeeFlag f where f.tenantId = :tenantId
            and (:employeeId is null or f.employeeId = :employeeId)
            and (:status is null or f.status = :status)
            order by f.status asc, f.createdAt desc, f.id
            """)
    List<EmployeeFlag> findFlags(@Param("tenantId") UUID tenantId, @Param("employeeId") UUID employeeId,
            @Param("status") EmployeeFlagStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from EmployeeFlag f where f.id = :id and f.tenantId = :tenantId")
    Optional<EmployeeFlag> findForResolution(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
