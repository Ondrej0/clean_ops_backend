package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateSiteRequest;
import com.example.backend_clean_ops.dto.responses.CreateSiteResponse;
import com.example.backend_clean_ops.dto.responses.GetSitesResponse;
import com.example.backend_clean_ops.dto.responses.SiteResponse;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.enums.SiteStatus;
import com.example.backend_clean_ops.repository.SiteRepository;
import com.example.backend_clean_ops.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Creates sites for an existing tenant and maps them into API responses.
@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final TenantRepository tenantRepository;

    public CreateSiteResponse createSite(CreateSiteRequest request) {
        Site site = new Site();
        Tenant tenant = tenantRepository.findById(request.tenantId())
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

    public GetSitesResponse getSites(UUID tenantId) {

        List<Site> sites = siteRepository.findAllByTenantIdAndStatus(tenantId, SiteStatus.ACTIVE);

        List<SiteResponse> siteResponses = new ArrayList<>();

        for(Site site: sites){
            SiteResponse siteResponse = new SiteResponse(
                    site.getId(), site.getName(), site.getAddressLine1(), site.getCity(), site.getPostcode(), site.getStatus()
            );

            siteResponses.add(siteResponse);
        }

        return new GetSitesResponse(siteResponses);

    }
}
