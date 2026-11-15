package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateSiteRequest;
import com.example.backend_clean_ops.dto.request.EditSiteRequest;
import com.example.backend_clean_ops.dto.responses.CreateSiteResponse;
import com.example.backend_clean_ops.dto.responses.GetSitesResponse;
import com.example.backend_clean_ops.dto.responses.getSite.GetSiteByIdResponse;
import com.example.backend_clean_ops.enums.SiteStatus;
import com.example.backend_clean_ops.service.SiteService;
import jakarta.validation.Valid;
import com.example.backend_clean_ops.service.ResourceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Exposes site creation endpoints tied to tenant setup.
@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {
    private final ResourceAccess access;

    private final SiteService siteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSiteResponse createSite(@RequestBody CreateSiteRequest request) {
        access.manager();
        request = new CreateSiteRequest(access.tenant(request.tenantId()), request.name(), request.addressLine1(), request.city(), request.postcode());
        return siteService.createSite(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public GetSitesResponse getSites(
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) SiteStatus status
            ){
        tenantId = access.tenant(tenantId);
        userId = access.employee(userId);
        return siteService.getSites(tenantId, userId, status);
    }

    @GetMapping("/{siteId}")
    @ResponseStatus(HttpStatus.OK)
    public GetSiteByIdResponse getSiteById(@PathVariable UUID siteId) {
        access.site(siteId);
        return siteService.getSiteById(siteId);
    }

    @PutMapping("/{siteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editSiteById(@PathVariable UUID siteId, @Valid @RequestBody EditSiteRequest request) {
        access.manager();
        access.site(siteId);
        request = new EditSiteRequest(access.tenant(request.tenantId()), request.name(), request.addressLine1(), request.addressLine2(), request.city(), request.postcode(), request.contactName(), request.contactPhone(), request.contactEmail(), request.hourlyRate());
        siteService.editSiteById(siteId, request);
    }

}
