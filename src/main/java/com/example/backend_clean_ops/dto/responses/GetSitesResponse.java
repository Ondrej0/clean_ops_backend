package com.example.backend_clean_ops.dto.responses;

import java.util.List;

public record GetSitesResponse(
        List<SiteResponse> Sites
) {
}
