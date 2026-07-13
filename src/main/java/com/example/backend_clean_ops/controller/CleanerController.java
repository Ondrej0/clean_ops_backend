package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateUserRequest;
import com.example.backend_clean_ops.dto.responses.CreateUserResponse;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Exposes cleaner onboarding endpoints backed by the shared user service.
@RestController
@RequestMapping("/api/cleaner")
@RequiredArgsConstructor
public class CleanerController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse createCleaner(@RequestBody CreateUserRequest request) {
        return userService.createUser(request, UserRole.CLEANER);
    }
}
