package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByTenantIdAndEmailIgnoreCaseAndActiveTrue(UUID tenantId, String email);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.id = :id and u.tenant.id = :tenantId")
    java.util.Optional<User> lockForFlagReport(@org.springframework.data.repository.query.Param("id") UUID id,
            @org.springframework.data.repository.query.Param("tenantId") UUID tenantId);
    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
    List<User> findAllByTenantIdAndRole(UUID tenantId, UserRole role);

    List<User> findAllByTenantIdAndRoleAndActiveTrue(UUID tenantId, UserRole role);
}
