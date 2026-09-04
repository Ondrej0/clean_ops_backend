package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateUserRequest;
import com.example.backend_clean_ops.dto.responses.CleanerResponse;
import com.example.backend_clean_ops.dto.responses.CreateUserResponse;
import com.example.backend_clean_ops.dto.responses.GetCleanersResponse;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.repository.TenantRepository;
import com.example.backend_clean_ops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Creates tenant-scoped users and returns a compact creation response.
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public CreateUserResponse createUser(CreateUserRequest request, UserRole userRole) {
        User user = new User();
        Tenant tenant =  tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        user.setTenant(tenant);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        //TODO take password as test and hash it
        user.setPasswordHash(request.passwordHash());
        user.setEmail(request.email());
        user.setRole(userRole);

        User savedUser = userRepository.save(user);

        return new CreateUserResponse(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getCreatedAt()
        );
    }

    public GetCleanersResponse getCleaners(UUID tenantId){
        List<User> cleaners = userRepository.findAllByTenantIdAndRole(tenantId, UserRole.CLEANER);

        List<CleanerResponse> cleanerResponses = new ArrayList<>();

        for(User cleaner: cleaners){
            CleanerResponse cleanerResponse = new CleanerResponse(
                    cleaner.getId(), cleaner.getFirstName(), cleaner.getLastName(), cleaner.getEmail(), cleaner.getPhone()
            );

            cleanerResponses.add(cleanerResponse);
        }

        return new GetCleanersResponse(cleanerResponses);
    }
}
