package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByTenantIdAndRole(UUID tenantId, UserRole role);

    List<User> findAllByTenantIdAndRoleAndActiveTrue(UUID tenantId, UserRole role);
}
