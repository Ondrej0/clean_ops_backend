package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.request.CreateSiteRequest;
import com.example.backend_clean_ops.dto.responses.SiteResponse;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;

    public SiteResponse createSite(CreateSiteRequest request) {
        Site site = new Site();

        site.setName(request.name());
        site.setAddressLine1(request.addressLine1());
        site.setCity(request.city());
        site.setPostcode(request.postcode());

        Site savedSite = siteRepository.save(site);

        return new SiteResponse(
                savedSite.getId(),
                savedSite.getName(),
                savedSite.getAddressLine1(),
                savedSite.getAddressLine2(),
                savedSite.getCity(),
                savedSite.getPostcode(),
                savedSite.getContactName(),
                savedSite.getContactPhone(),
                savedSite.getContactEmail(),
                savedSite.getStatus(),
                savedSite.getHourlyRate(),
                savedSite.getCreatedAt(),
                savedSite.getUpdatedAt()
        );
    }
}