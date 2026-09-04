package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.request.CreateUserRequest;
import com.example.backend_clean_ops.dto.responses.CreateUserResponse;
import com.example.backend_clean_ops.dto.responses.GetCleanersResponse;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public GetCleanersResponse getCleaners(@RequestParam UUID tenantId){
        return userService.getCleaners(tenantId);
    }
//TODO finish this method and finish teh response object too -- GetCleanerDataResponse
//    @GetMapping("/{userId")
//    @ResponseStatus(HttpStatus.OK)
//    public

}
