package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateUserRequest;
import com.example.backend_clean_ops.dto.responses.CreateUserResponse;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.repository.TenantRepository;
import com.example.backend_clean_ops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Creates tenant-scoped users and returns a compact creation response.
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public CreateUserResponse createUser(CreateUserRequest request, UserRole userRole) {
        User user = new User();
        Tenant tenant =  tenantRepository.findById(request.tenantID())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        user.setTenant(tenant);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPasswordHash(request.passwordHash());
        user.setRole(userRole);

        User savedUser = userRepository.save(user);

        return new CreateUserResponse(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getCreatedAt()
        );
    }
}
