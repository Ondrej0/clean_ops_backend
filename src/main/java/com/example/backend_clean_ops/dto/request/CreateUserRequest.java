package com.example.backend_clean_ops.dto.request;

import java.util.UUID;

public record CreateUserRequest(
       UUID tenantID,
       String firstName,
       String lastName,
       String passwordHash,
       Float payRate
) {}
