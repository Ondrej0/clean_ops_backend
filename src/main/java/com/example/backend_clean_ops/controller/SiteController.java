package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateSiteRequest;
import com.example.backend_clean_ops.dto.responses.CreateSiteResponse;
import com.example.backend_clean_ops.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}
