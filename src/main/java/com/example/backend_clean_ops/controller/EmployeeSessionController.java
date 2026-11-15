package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.service.CurrentEmployee;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequiredArgsConstructor
public class EmployeeSessionController {
    private final CurrentEmployee current;

    public record SessionUser(UUID id, UUID tenantId, String name, UserRole role) {}

    @GetMapping("/api/session/csrf")
    public Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken()); }

    @GetMapping("/api/session/me") @Transactional(readOnly = true)
    public SessionUser me() {
        var user = current.get();
        return new SessionUser(user.getId(), user.getTenant().getId(),
                user.getFirstName() + " " + user.getLastName(), user.getRole());
    }

}
