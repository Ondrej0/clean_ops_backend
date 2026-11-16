package com.example.backend_clean_ops.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateShiftRequest(
        @NotNull UUID siteId,
        @NotNull UUID userId,
        @NotNull LocalDateTime scheduledStart,
        @NotNull LocalDateTime scheduledEnd
) {}
