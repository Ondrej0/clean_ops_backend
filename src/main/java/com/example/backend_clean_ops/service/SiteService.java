package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateSiteRequest;
import com.example.backend_clean_ops.dto.responses.CreateSiteResponse;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.repository.SiteRepository;
import com.example.backend_clean_ops.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final TenantRepository tenantRepository;

    public CreateSiteResponse createSite(CreateSiteRequest request) {
        Site site = new Site();
        Tenant tenant = tenantRepository.findById(request.tenantID())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        site.setTenant(tenant);
        site.setName(request.name());
        site.setAddressLine1(request.addressLine1());
        site.setCity(request.city());
        site.setPostcode(request.postcode());

        Site savedSite = siteRepository.save(site);

        return new CreateSiteResponse(
                savedSite.getId(),
                savedSite.getName(),
                savedSite.getCreatedAt()
        );
    }
}