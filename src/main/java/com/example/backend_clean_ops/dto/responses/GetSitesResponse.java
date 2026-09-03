package com.example.backend_clean_ops.dto.responses;

import java.util.List;
//TODO change sites to be small S, also have to update fronet end so tehre is a match
public record GetSitesResponse(
        List<SiteResponse> Sites
) {
}
