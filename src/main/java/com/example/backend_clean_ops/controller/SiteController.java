package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateSiteRequest;
import com.example.backend_clean_ops.dto.request.EditSiteRequest;
import com.example.backend_clean_ops.dto.responses.CreateSiteResponse;
import com.example.backend_clean_ops.dto.responses.GetSitesResponse;
import com.example.backend_clean_ops.dto.responses.getSite.GetSiteByIdResponse;
import com.example.backend_clean_ops.enums.SiteStatus;
import com.example.backend_clean_ops.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Exposes site creation endpoints tied to tenant setup.
@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSiteResponse createSite(@RequestBody CreateSiteRequest request) {
        return siteService.createSite(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public GetSitesResponse getSites(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) SiteStatus status
            ){
        return siteService.getSites(tenantId, userId, status);
    }

    @GetMapping("/{siteId}")
    @ResponseStatus(HttpStatus.OK)
    public GetSiteByIdResponse getSiteById(@PathVariable UUID siteId) {
        return siteService.getSiteById(siteId);
    }

    @PutMapping("/{siteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editSiteById(@PathVariable UUID siteId, @Valid @RequestBody EditSiteRequest request) {
        siteService.editSiteById(siteId, request);
    }

}
