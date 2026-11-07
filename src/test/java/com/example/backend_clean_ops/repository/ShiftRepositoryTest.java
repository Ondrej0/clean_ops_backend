package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.*;
import com.example.backend_clean_ops.enums.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class ShiftRepositoryTest {
    @Autowired private ShiftRepository repository;
    @Autowired private EntityManager entityManager;

    @Test
    void windowIncludesOvernightAndAllStatusesButExcludesOtherUsersTenantsAndOutsideDates() {
        Tenant tenant = persistTenant("Tenant");
        User user = persistUser(tenant);
        Site site = persistSite(tenant, "Office", SiteStatus.ACTIVE);
        LocalDateTime from = LocalDateTime.parse("2026-11-04T00:00:00");
        LocalDateTime until = LocalDateTime.parse("2026-11-11T00:00:00");
        Shift overnight = persistShift(tenant, user, site, from.minusHours(2), from.plusHours(2), ShiftStatus.COMPLETED);
        Shift cancelled = persistShift(tenant, user, site, from.plusDays(1), from.plusDays(1).plusHours(2), ShiftStatus.CANCELLED);
        Shift noShow = persistShift(tenant, user, site, from.plusDays(2), from.plusDays(2).plusHours(2), ShiftStatus.NO_SHOW);
        Shift upcoming = persistShift(tenant, user, site, until.minusHours(1), until.plusHours(2), ShiftStatus.SCHEDULED);
        persistShift(tenant, user, site, from.minusHours(3), from, ShiftStatus.COMPLETED);
        persistShift(tenant, user, site, until, until.plusHours(2), ShiftStatus.SCHEDULED);
        persistShift(tenant, persistUser(tenant), site, from, from.plusHours(2), ShiftStatus.SCHEDULED);
        Tenant other = persistTenant("Other tenant");
        persistShift(other, persistUser(other), persistSite(other, "Other", SiteStatus.ACTIVE), from, from.plusHours(2), ShiftStatus.SCHEDULED);
        entityManager.flush();
        entityManager.clear();
        assertThat(repository.findInWindow(tenant.getId(), user.getId(), from, until))
                .extracting(Shift::getId).containsExactly(overnight.getId(), cancelled.getId(), noShow.getId(), upcoming.getId());
        assertThat(repository.findInWindow(other.getId(), user.getId(), from, until)).isEmpty();
        assertThat(repository.findForAttendance(upcoming.getId())).isPresent();
    }

    private Shift persistShift(Tenant tenant, User user, Site site, LocalDateTime start, LocalDateTime end, ShiftStatus status) {
        Shift shift = Shift.builder().tenant(tenant).user(user).site(site).shiftDate(start.toLocalDate())
                .scheduledStart(start).scheduledEnd(end).status(status).build();
        entityManager.persist(shift);
        return shift;
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

}
