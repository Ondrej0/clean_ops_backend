package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.UserRole;
import com.example.backend_clean_ops.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.UUID;

/** Shared authorization for resource controllers. IDs from requests never establish identity. */
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ResourceAccess {
    private final CurrentEmployee current;
    private final UserRepository users;
    private final SiteRepository sites;
    private final ScheduleRepository schedules;
    private final ScheduleAssignmentRepository assignments;
    public void manager() { current.require(UserRole.MANAGER); }
    public UUID tenant(UUID supplied) {
        UUID actual = current.get().getTenant().getId();
        if (supplied != null && !actual.equals(supplied)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return actual;
    }
    public UUID employee(UUID requested) {
        User actor = current.get();
        if (actor.getRole() == UserRole.CLEANER) {
            if (requested != null && !requested.equals(actor.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            return actor.getId();
        }
        if (requested != null && !users.existsByIdAndTenantId(requested, actor.getTenant().getId())) missing();
        return requested;
    }
    public UUID cleaner(UUID supplied) {
        current.require(UserRole.CLEANER);
        return employee(supplied);
    }
    public void site(UUID id) {
        User actor = current.get();
        var site = sites.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!site.getTenant().getId().equals(actor.getTenant().getId())) missing();
        if (actor.getRole() == UserRole.CLEANER && assignments.findByUserIdWithScheduleAndSite(actor.getId()).stream()
                .noneMatch(a -> a.getSchedule().isActive() && a.getSchedule().getSite().getId().equals(id))) missing();
    }
    public void schedule(UUID id) {
        User actor = current.get();
        var schedule = schedules.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!schedule.getTenant().getId().equals(actor.getTenant().getId())) missing();
        if (actor.getRole() == UserRole.CLEANER && !assignments.existsByScheduleIdAndUserId(id, actor.getId())) missing();
    }
    private void missing() { throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
}
