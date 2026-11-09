package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.*;
import com.example.backend_clean_ops.enums.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findActiveBySiteIdAndUserId_shouldFilterAssignmentsWithoutReturningOtherSchedules() {
        Tenant tenant = persistTenant();
        Site site = persistSite(tenant);
        Site otherSite = persistSite(tenant);
        Site otherTenantSite = persistSite(persistTenant());
        User cleaner = persistUser(tenant);
        User otherCleaner = persistUser(tenant);
        User otherTenantCleaner = persistUser(otherTenantSite.getTenant());
        Schedule assigned = persistSchedule(site, "Assigned", true);
        Schedule shared = persistSchedule(site, "Shared", true);
        Schedule otherCleanerSchedule = persistSchedule(site, "Other cleaner", true);
        persistSchedule(site, "Unassigned", true);
        assign(assigned, cleaner);
        assign(shared, cleaner);
        assign(shared, otherCleaner);
        assign(otherCleanerSchedule, otherCleaner);
        assign(persistSchedule(site, "Inactive", false), cleaner);
        assign(persistSchedule(otherSite, "Other site", true), cleaner);
        assign(persistSchedule(otherTenantSite, "Other tenant", true), otherTenantCleaner);
        entityManager.flush();
        entityManager.clear();

        assertThat(scheduleRepository.findActiveBySiteIdAndUserId(site.getId(), cleaner.getId()))
                .extracting(Schedule::getName).containsExactlyInAnyOrder("Assigned", "Shared");
        assertThat(scheduleRepository.findActiveBySiteIdAndUserId(site.getId(), null))
                .extracting(Schedule::getName)
                .containsExactlyInAnyOrder("Assigned", "Shared", "Other cleaner", "Unassigned");
        assertThat(scheduleRepository.findActiveBySiteIdAndUserId(site.getId(), UUID.randomUUID()))
                .isEmpty();
        assertThat(scheduleRepository.findActiveBySiteIdAndUserId(site.getId(), otherTenantCleaner.getId()))
                .isEmpty();
    }

    private Tenant persistTenant() {
        Tenant tenant = Tenant.builder()
                .name("Tenant").contactName("Pat Smith").contactEmail("pat@example.com")
                .addressLine1("12 High Street").city("London").postcode("SW1A 1AA").build();
        entityManager.persist(tenant);
        return tenant;
    }

    private Site persistSite(Tenant tenant) {
        Site site = Site.builder().tenant(tenant).name("Office")
                .addressLine1("12 High Street").city("London").postcode("SW1A 1AA").build();
        entityManager.persist(site);
        return site;
    }

    private User persistUser(Tenant tenant) {
        User user = User.builder().tenant(tenant).firstName("Alex").lastName("Jones")
                .email(UUID.randomUUID() + "@example.com").passwordHash("test-hash")
                .role(UserRole.CLEANER).build();
        entityManager.persist(user);
        return user;
    }

    private Schedule persistSchedule(Site site, String name, boolean active) {
        Schedule schedule = Schedule.builder().tenant(site.getTenant()).site(site)
                .name(name).active(active).build();
        entityManager.persist(schedule);
        return schedule;
    }

    private void assign(Schedule schedule, User user) {
        entityManager.persist(ScheduleAssignment.builder().tenant(schedule.getTenant())
                .schedule(schedule).user(user).build());
    }
}
