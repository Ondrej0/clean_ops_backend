package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateUserRequest;
import com.example.backend_clean_ops.dto.request.EditEmployeeRequest;
import com.example.backend_clean_ops.dto.responses.CleanerResponse;
import com.example.backend_clean_ops.dto.responses.CreateUserResponse;
import com.example.backend_clean_ops.dto.responses.AssignedScheduleResponse;
import com.example.backend_clean_ops.dto.responses.AssignedSiteResponse;
import com.example.backend_clean_ops.dto.responses.GetCleanerDataResponse;
import com.example.backend_clean_ops.dto.responses.GetCleanersResponse;
import com.example.backend_clean_ops.entity.ScheduleAssignment;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.repository.TenantRepository;
import com.example.backend_clean_ops.repository.ScheduleAssignmentRepository;
import com.example.backend_clean_ops.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

// Creates tenant-scoped users and returns a compact creation response.
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final ScheduleAssignmentRepository scheduleAssignmentRepository;

    public CreateUserResponse createUser(CreateUserRequest request, UserRole userRole) {
        User user = new User();
        Tenant tenant =  tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        user.setTenant(tenant);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPasswordHash(EmployeePasswords.hashLegacyValue(request.passwordHash()));
        user.setEmail(request.email());
        user.setPayRate(request.payRate());
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
        List<User> cleaners = userRepository.findAllByTenantIdAndRoleAndActiveTrue(tenantId, UserRole.CLEANER);

        List<CleanerResponse> cleanerResponses = new ArrayList<>();

        for(User cleaner: cleaners){
            CleanerResponse cleanerResponse = new CleanerResponse(
                    cleaner.getId(), cleaner.getFirstName(), cleaner.getLastName(), cleaner.getEmail(), cleaner.getPhone()
            );

            cleanerResponses.add(cleanerResponse);
        }

        return new GetCleanersResponse(cleanerResponses);
    }

    public GetCleanerDataResponse getCleanerData(UUID cleanerId) {
        User cleaner = userRepository.findById(cleanerId)
                .filter(user -> user.getRole() == UserRole.CLEANER)
                .orElseThrow(() -> new RuntimeException("Cleaner not found"));

        List<ScheduleAssignment> assignments = scheduleAssignmentRepository
                .findByUserIdWithScheduleAndSite(cleanerId);
        List<AssignedScheduleResponse> assignedSchedules = assignments.stream()
                .map(ScheduleAssignment::getSchedule)
                .map(schedule -> new AssignedScheduleResponse(schedule.getId(), schedule.getName()))
                .toList();

        Map<UUID, AssignedSiteResponse> assignedSitesById = new LinkedHashMap<>();
        for (ScheduleAssignment assignment : assignments) {
            var site = assignment.getSchedule().getSite();
            assignedSitesById.putIfAbsent(site.getId(), new AssignedSiteResponse(
                    site.getId(),
                    site.getName(),
                    site.getAddressLine1(),
                    site.getPostcode()
            ));
        }

        return new GetCleanerDataResponse(
                cleaner.getId(),
                cleaner.getFirstName(),
                cleaner.getLastName(),
                cleaner.getEmail(),
                cleaner.getPhone(),
                cleaner.getAddress(),
                cleaner.getRole(),
                cleaner.getPayRate(),
                cleaner.isActive(),
                cleaner.getCreatedAt(),
                cleaner.getUpdatedAt(),
                assignedSchedules,
                List.copyOf(assignedSitesById.values())
        );
    }

    @Transactional
    public void editEmployeeById(UUID employeeId, EditEmployeeRequest request) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getTenant().getId().equals(request.tenantId())) {
            throw new RuntimeException("Employee does not belong to tenant");
        }

        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPhone(request.phone());
        // Older manager clients omit address; an empty string explicitly clears it.
        if (request.address() != null) {
            employee.setAddress(request.address().trim().isEmpty() ? null : request.address().trim());
        }
        employee.setRole(request.role());
        employee.setPayRate(request.payRate());

        userRepository.save(employee);
    }
}
