package com.example.backend_clean_ops.dto.responses;


import java.util.List;

public record GetCleanersResponse(
        List<CleanerResponse> cleaners
) {
}
