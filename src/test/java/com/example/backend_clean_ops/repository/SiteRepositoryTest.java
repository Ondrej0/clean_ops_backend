package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.Schedule;
import com.example.backend_clean_ops.entity.ScheduleAssignment;
import com.example.backend_clean_ops.entity.Site;
import com.example.backend_clean_ops.entity.SiteAssignment;
import com.example.backend_clean_ops.entity.Tenant;
import com.example.backend_clean_ops.entity.User;
import com.example.backend_clean_ops.enums.SiteStatus;
import com.example.backend_clean_ops.enums.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class SiteRepositoryTest {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        Tenant tenant = persistTenant("First tenant");
        Tenant otherTenant = persistTenant("Other tenant");
        User user = persistUser(tenant);
        User otherUser = persistUser(tenant);
        User otherTenantUser = persistUser(otherTenant);
        tenantId = tenant.getId();
        userId = user.getId();

        Site assignedActive = persistSite(tenant, "Assigned active", SiteStatus.ACTIVE);
        Site assignedInactive = persistSite(tenant, "Assigned inactive", SiteStatus.INACTIVE);
        Site otherUserSite = persistSite(tenant, "Other user's site", SiteStatus.ACTIVE);
        Site directOnly = persistSite(tenant, "Direct assignment only", SiteStatus.ACTIVE);
        persistSite(tenant, "Unassigned inactive", SiteStatus.INACTIVE);
        Site otherTenantSite = persistSite(otherTenant, "Other tenant's site", SiteStatus.ACTIVE);

        // Multiple schedules for one user and site must still yield a single site.
        persistScheduleAssignment(assignedActive, user);
        persistScheduleAssignment(assignedActive, user);
        persistScheduleAssignment(assignedInactive, user);
        persistScheduleAssignment(otherUserSite, otherUser);
        persistScheduleAssignment(otherTenantSite, otherTenantUser);

        // A direct site assignment does not establish membership for this filter.
        entityManager.persist(SiteAssignment.builder()
                .tenant(tenant).site(directOnly).user(user).build());
        entityManager.flush();
        entityManager.clear();
    }

    @ParameterizedTest
    @MethodSource("siteFilters")
    void findByFilters_shouldCombineOptionalFiltersWithinTenant(
            boolean filterByUser, SiteStatus status, List<String> expectedNames) {
        List<Site> sites = siteRepository.findByFilters(tenantId, filterByUser ? userId : null, status);

        assertThat(sites).extracting(Site::getName).containsExactlyInAnyOrderElementsOf(expectedNames);
    }

    @Test
    void findByFilters_whenUserHasNoScheduleAssignments_shouldReturnEmptyList() {
        User unassignedUser = persistUser(entityManager.getReference(Tenant.class, tenantId));
        entityManager.flush();

        assertThat(siteRepository.findByFilters(tenantId, unassignedUser.getId(), null)).isEmpty();
        assertThat(siteRepository.findByFilters(tenantId, UUID.randomUUID(), null)).isEmpty();
    }

    @Test
    void findByFilters_whenUserBelongsToAnotherTenant_shouldReturnEmptyList() {
        Tenant otherTenant = persistTenant("Requested tenant");

        assertThat(siteRepository.findByFilters(otherTenant.getId(), userId, null)).isEmpty();
    }

    @Test
    void findByFilters_whenTenantDoesNotExist_shouldReturnEmptyList() {
        assertThat(siteRepository.findByFilters(UUID.randomUUID(), null, null)).isEmpty();
    }

    private static Stream<Arguments> siteFilters() {
        return Stream.of(
                Arguments.of(false, null, List.of("Assigned active", "Assigned inactive",
                        "Other user's site", "Direct assignment only", "Unassigned inactive")),
                Arguments.of(false, SiteStatus.ACTIVE, List.of("Assigned active",
                        "Other user's site", "Direct assignment only")),
                Arguments.of(false, SiteStatus.INACTIVE, List.of("Assigned inactive", "Unassigned inactive")),
                Arguments.of(true, null, List.of("Assigned active", "Assigned inactive")),
                Arguments.of(true, SiteStatus.ACTIVE, List.of("Assigned active")),
                Arguments.of(true, SiteStatus.INACTIVE, List.of("Assigned inactive"))
        );
    }

    private Tenant persistTenant(String name) {
        Tenant tenant = Tenant.builder()
                .name(name).contactName("Pat Smith").contactEmail("pat@example.com")
                .addressLine1("12 High Street").city("London").postcode("SW1A 1AA").build();
        entityManager.persist(tenant);
        return tenant;
    }

    private User persistUser(Tenant tenant) {
        User user = User.builder()
                .tenant(tenant).firstName("Alex").lastName("Jones")
                .email(UUID.randomUUID() + "@example.com").passwordHash("test-hash")
                .role(UserRole.CLEANER).build();
        entityManager.persist(user);
        return user;
    }

    private Site persistSite(Tenant tenant, String name, SiteStatus status) {
        Site site = Site.builder()
                .tenant(tenant).name(name).status(status)
                .addressLine1("12 High Street").city("London").postcode("SW1A 1AA").build();
        entityManager.persist(site);
        return site;
    }

    private void persistScheduleAssignment(Site site, User user) {
        Schedule schedule = Schedule.builder()
                .tenant(site.getTenant()).site(site).name("Morning cleaning").build();
        entityManager.persist(schedule);
        entityManager.persist(ScheduleAssignment.builder()
                .tenant(site.getTenant()).schedule(schedule).user(user).build());
    }
}
