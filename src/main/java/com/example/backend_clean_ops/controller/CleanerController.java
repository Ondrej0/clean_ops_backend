package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateUserRequest;
import com.example.backend_clean_ops.dto.request.EditEmployeeRequest;
import com.example.backend_clean_ops.dto.responses.CreateUserResponse;
import com.example.backend_clean_ops.dto.responses.GetCleanersResponse;
import com.example.backend_clean_ops.dto.responses.GetCleanerDataResponse;
import com.example.backend_clean_ops.service.EmployeeAccountService;
import com.example.backend_clean_ops.service.UserService;
import jakarta.validation.Valid;
import com.example.backend_clean_ops.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Exposes cleaner onboarding endpoints backed by the shared user service.
@RestController
@RequestMapping("/api/cleaner")
@RequiredArgsConstructor
public class CleanerController {
    private final ResourceAccess access;

    private final UserService userService;
    private final EmployeeAccountService accounts;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse createCleaner(@RequestBody CreateUserRequest request) {
        access.manager();
        request = new CreateUserRequest(access.tenant(request.tenantId()), request.firstName(), request.lastName(), request.passwordHash(), request.email(), request.payRate());
        return accounts.createCleaner(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public GetCleanersResponse getCleaners(@RequestParam(required = false) UUID tenantId){
        access.manager();
        tenantId = access.tenant(tenantId);
        return userService.getCleaners(tenantId);
    }

    @GetMapping("/{cleanerId}")
    @ResponseStatus(HttpStatus.OK)
    public GetCleanerDataResponse getCleanerData(@PathVariable UUID cleanerId) {
        access.employee(cleanerId);
        return userService.getCleanerData(cleanerId);
    }

    @PutMapping("/{employeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editEmployeeById(@PathVariable UUID employeeId, @Valid @RequestBody EditEmployeeRequest request) {
        request = new EditEmployeeRequest(access.tenant(request.tenantId()), request.firstName(), request.lastName(), request.email(), request.phone(), request.role(), request.payRate(), request.address());
        accounts.edit(employeeId, request);
    }

}
