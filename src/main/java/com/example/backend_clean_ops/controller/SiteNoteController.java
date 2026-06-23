package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.service.SiteNoteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/site-notes")
public class SiteNoteController {

    private final SiteNoteService siteNoteService;

    public SiteNoteController(SiteNoteService siteNoteService) {
        this.siteNoteService = siteNoteService;
    }
}
