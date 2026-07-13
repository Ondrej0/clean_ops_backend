package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateTenantRequest;
import com.example.backend_clean_ops.dto.responses.CreateTenantResponse;
import com.example.backend_clean_ops.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Exposes tenant creation endpoints for onboarding and setup flows.
@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTenantResponse createTenant(@RequestBody CreateTenantRequest createTenantRequest) {
        return tenantService.createTenant(createTenantRequest);
    }

}
