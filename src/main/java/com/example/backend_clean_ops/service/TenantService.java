package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateTenantRequest;
import com.example.backend_clean_ops.dto.responses.CreateTenantResponse;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Creates tenants from request data and returns the generated identifier payload.
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public CreateTenantResponse createTenant(CreateTenantRequest request) {
        Tenant tenant = new Tenant();

        tenant.setName(request.name());
        tenant.setContactName(request.contactName());
        tenant.setContactEmail(request.contactEmail());
        tenant.setAddressLine1(request.addressLine1());
        tenant.setCity(request.city());
        tenant.setPostcode(request.postcode());

        Tenant createdTenant = tenantRepository.save(tenant);

        return new CreateTenantResponse(
                createdTenant.getId(),
                createdTenant.getName(),
                createdTenant.getCreatedAt()
        );
    }

}
