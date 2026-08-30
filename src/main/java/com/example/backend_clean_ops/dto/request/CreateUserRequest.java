package com.example.backend_clean_ops.dto.request;

import java.util.UUID;

public record CreateUserRequest(
       UUID tenantID,
       String firstName,
       String lastName,
       //TODO this should be password as text and get hashed in the backend
       String passwordHash,
       String  email,
       Float payRate
) {}
