package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.*;
import com.example.backend_clean_ops.dto.responses.CreateUserResponse;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

/** Guard identity writes so legacy employee endpoints cannot bypass flag authorization. */
@Service @RequiredArgsConstructor
public class EmployeeAccountService {
    private final CurrentEmployee current;
    private final UserRepository users;
    private final UserService service;

    @Transactional
    public CreateUserResponse createCleaner(CreateUserRequest request) {
        var actor = current.require(UserRole.MANAGER);
        if (!actor.getTenant().getId().equals(request.tenantId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (request.passwordHash() == null || request.passwordHash().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A temporary password is required.");
        return service.createUser(request, UserRole.CLEANER);
    }

    @Transactional
    public void edit(UUID id, EditEmployeeRequest request) {
        var actor = current.get();
        var employee = users.findById(id)
                .filter(u -> u.getTenant().getId().equals(actor.getTenant().getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!actor.getTenant().getId().equals(request.tenantId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (actor.getRole() != UserRole.MANAGER) {
            boolean samePay = employee.getPayRate() == null ? request.payRate() == null
                    : request.payRate() != null && employee.getPayRate().compareTo(request.payRate()) == 0;
            if (!actor.getId().equals(id) || employee.getRole() != request.role() || !samePay)
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        service.editEmployeeById(id, request);
    }
}
