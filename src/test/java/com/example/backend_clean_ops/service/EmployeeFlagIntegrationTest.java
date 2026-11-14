package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.entity.*;
import com.example.backend_clean_ops.enums.*;
import com.example.backend_clean_ops.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.flyway.enabled=false", "spring.docker.compose.enabled=false"})
@Transactional
class EmployeeFlagIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired EntityManager em;
    @Autowired EmployeeFlagRepository flags;
    @Autowired EmployeeFlagService service;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
    private MockMvc mvc;
    private Tenant tenant, otherTenant;
    private User cleaner, colleague, manager, outsider;
    private Site site;
    private static final String HASH = new BCryptPasswordEncoder().encode("test-password");
    private static final String REPORT = "{\"fromDate\":\"2026-11-09\",\"toDate\":\"2026-11-15\"}";

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        if (!org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
            new org.springframework.transaction.support.TransactionTemplate(transactions).executeWithoutResult(status -> seed());
        } else seed();
    }

    private void seed() {
        tenant = tenant("Company"); otherTenant = tenant("Other company");
        cleaner = createUser(tenant, UserRole.CLEANER); colleague = createUser(tenant, UserRole.CLEANER);
        manager = createUser(tenant, UserRole.MANAGER); outsider = createUser(otherTenant, UserRole.MANAGER);
        site = site(tenant);
    }

    @Test
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void concurrentReportsCreateExactlyOneOpenFlag() throws Exception {
        var transaction = new org.springframework.transaction.support.TransactionTemplate(transactions);
        transaction.executeWithoutResult(status -> overlap());
        var ready = new java.util.concurrent.CountDownLatch(2);
        var start = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<EmployeeFlagService.ReportResult> report = () -> {
                var security = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
                security.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        cleaner.getId().toString(), "", List.of()));
                org.springframework.security.core.context.SecurityContextHolder.setContext(security);
                try {
                    ready.countDown();
                    if (!start.await(10, java.util.concurrent.TimeUnit.SECONDS)) throw new IllegalStateException("Start timeout");
                    return service.report(LocalDate.parse("2026-11-09"), LocalDate.parse("2026-11-15"));
                } finally { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
            };
            var first = executor.submit(report); var second = executor.submit(report);
            assertThat(ready.await(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue(); start.countDown();
            var a = first.get(20, java.util.concurrent.TimeUnit.SECONDS);
            var b = second.get(20, java.util.concurrent.TimeUnit.SECONDS);
            assertThat(a.created() + b.created()).isEqualTo(1);
            assertThat(a.alreadyReported() + b.alreadyReported()).isEqualTo(1);
            assertThat(flags.findFlags(tenant.getId(), cleaner.getId(), EmployeeFlagStatus.OPEN)).hasSize(1);
        } finally {
            transaction.executeWithoutResult(status -> {
                em.createQuery("delete from EmployeeFlag f where f.tenantId = :tenant").setParameter("tenant", tenant.getId()).executeUpdate();
                for (String entity : List.of("Shift", "Site", "User"))
                    em.createQuery("delete from " + entity + " e where e.tenant.id in :tenants")
                            .setParameter("tenants", List.of(tenant.getId(), otherTenant.getId())).executeUpdate();
                em.createQuery("delete from Tenant t where t.id in :tenants")
                        .setParameter("tenants", List.of(tenant.getId(), otherTenant.getId())).executeUpdate();
            });
        }
    }

    @Test void reportsOwnOverlapsOnlyAndPreservesUsefulSnapshots() throws Exception {
        Shift first = shift(cleaner, "2026-11-09T09:00", "2026-11-09T12:00");
        Shift second = shift(cleaner, "2026-11-09T11:00", "2026-11-09T13:00");
        shift(colleague, "2026-11-09T10:00", "2026-11-09T14:00");
        report(cleaner).andExpect(status().isOk()).andExpect(jsonPath("$.detected").value(1))
                .andExpect(jsonPath("$.created").value(1)).andExpect(jsonPath("$.alreadyReported").value(0));
        var flag = flags.findAll().getFirst();
        assertThat(flag.getEmployeeId()).isEqualTo(cleaner.getId());
        assertThat(flag.getTenantId()).isEqualTo(tenant.getId());
        assertThat(flag.getMessage()).contains("Office", "Mon 09 Nov 2026 09:00", "13:00", "Europe/London");
        assertThat(Set.of(flag.getFirstShiftId(), flag.getSecondShiftId())).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(flag.getCreatedAt()).isNotNull();
        assertThat(flag.getStatus()).isEqualTo(EmployeeFlagStatus.OPEN);
    }

    @Test void emptyTouchingCancelledAndOutsideWindowShiftsDoNotCreateFlags() throws Exception {
        report(cleaner).andExpect(jsonPath("$.detected").value(0));
        shift(cleaner, "2026-11-09T09:00", "2026-11-09T10:00");
        shift(cleaner, "2026-11-09T10:00", "2026-11-09T11:00");
        shift(cleaner, "2026-11-09T09:00", "2026-11-09T11:00").setStatus(ShiftStatus.CANCELLED);
        shift(cleaner, "2026-11-16T09:00", "2026-11-16T11:00");
        report(cleaner).andExpect(jsonPath("$.created").value(0));
        assertThat(flags.count()).isZero();
    }

    @Test void deduplicatesAndReportsMultipleIndependentConflicts() throws Exception {
        overlap();
        shift(cleaner, "2026-11-10T20:00", "2026-11-11T02:00");
        shift(cleaner, "2026-11-11T01:00", "2026-11-11T03:00");
        report(cleaner).andExpect(jsonPath("$.created").value(2));
        report(cleaner).andExpect(jsonPath("$.created").value(0)).andExpect(jsonPath("$.alreadyReported").value(2));
        assertThat(flags.count()).isEqualTo(2);
    }

    @Test void includesPreviousWeekOvernightConflicts() throws Exception {
        shift(cleaner, "2026-11-08T22:00", "2026-11-09T02:00");
        shift(cleaner, "2026-11-09T01:00", "2026-11-09T03:00");
        report(cleaner).andExpect(jsonPath("$.created").value(1));
    }

    @Test void rejectsIdentityOverridesAndInvalidRanges() throws Exception {
        overlap();
        for (String name : List.of("employeeId", "userId", "tenantId")) {
            mvc.perform(post("/api/employee-flags/schedule-overlaps").with(user(cleaner.getId().toString())).with(csrf())
                    .contentType("application/json").content(REPORT.replace("}", ",\"" + name + "\":\"" + colleague.getId() + "\"}")))
                    .andExpect(status().isBadRequest());
            mvc.perform(post("/api/employee-flags/schedule-overlaps").with(user(cleaner.getId().toString())).with(csrf())
                    .param(name, colleague.getId().toString()).contentType("application/json").content(REPORT))
                    .andExpect(status().isBadRequest());
        }
        for (String body : List.of("{}", REPORT.replace("2026-11-15", "2026-11-08"), REPORT.replace("2026-11-15", "2027-11-15"))) {
            mvc.perform(post("/api/employee-flags/schedule-overlaps").with(user(cleaner.getId().toString())).with(csrf())
                    .contentType("application/json").content(body)).andExpect(status().isBadRequest());
        }
        assertThat(flags.count()).isZero();
    }

    @Test void managerListsTenantFlagsWithEmployeeAndStatusFilters() throws Exception {
        overlap(); report(cleaner);
        mvc.perform(get("/api/employee-flags").with(user(manager.getId().toString())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeName").value("Alex Smith"));
        mvc.perform(get("/api/employee-flags").param("employeeId", colleague.getId().toString()).with(user(manager.getId().toString())))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/employee-flags").param("status", "RESOLVED").with(user(manager.getId().toString())))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/employee-flags").with(user(outsider.getId().toString())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/employee-flags").param("employeeId", cleaner.getId().toString()).with(user(outsider.getId().toString())))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/employee-flags/mine").with(user(cleaner.getId().toString())))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/employee-flags/mine").with(user(colleague.getId().toString())))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test void resolutionIsTenantScopedIdempotentAndAllowsNewReportWithHistory() throws Exception {
        overlap(); report(cleaner);
        EmployeeFlag flag = flags.findAll().getFirst();
        mvc.perform(post("/api/employee-flags/{id}/resolve", flag.getId()).with(user(outsider.getId().toString())).with(csrf()))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/employee-flags/{id}/resolve", flag.getId()).with(user(manager.getId().toString())).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolvedBy").value(manager.getId().toString())).andExpect(jsonPath("$.resolvedAt").isNotEmpty());
        Instant resolved = flag.getResolvedAt();
        mvc.perform(post("/api/employee-flags/{id}/resolve", flag.getId()).with(user(manager.getId().toString())).with(csrf())).andExpect(status().isOk());
        assertThat(flag.getResolvedAt()).isEqualTo(resolved);
        assertThat(flag.getOpenConflictKey()).isNull();
        report(cleaner).andExpect(jsonPath("$.created").value(1));
        report(cleaner).andExpect(jsonPath("$.alreadyReported").value(1));
        assertThat(flags.count()).isEqualTo(2);
        mvc.perform(get("/api/employee-flags").with(user(manager.getId().toString())))
                .andExpect(jsonPath("$[0].status").value("OPEN")).andExpect(jsonPath("$[1].status").value("RESOLVED"));
    }

    @Test void scheduleRegenerationDoesNotDuplicateAndDeletionPreservesHistory() throws Exception {
        Schedule schedule = Schedule.builder().tenant(tenant).site(site).name("Weekly clean").build(); em.persist(schedule);
        Shift a = shift(cleaner, "2026-11-09T09:00", "2026-11-09T12:00"); a.setSchedule(schedule);
        Shift b = shift(cleaner, "2026-11-09T11:00", "2026-11-09T13:00");
        report(cleaner);
        EmployeeFlag original = flags.findAll().getFirst(); String message = original.getMessage();
        em.remove(a); em.flush();
        Shift replacement = shift(cleaner, "2026-11-09T09:00", "2026-11-09T12:00"); replacement.setSchedule(schedule);
        report(cleaner).andExpect(jsonPath("$.alreadyReported").value(1));
        replacement.setScheduledStart(LocalDateTime.parse("2026-11-09T13:00"));
        replacement.setScheduledEnd(LocalDateTime.parse("2026-11-09T14:00"));
        report(cleaner).andExpect(jsonPath("$.detected").value(0));
        em.remove(replacement); em.remove(b); em.remove(schedule); em.flush();
        assertThat(flags.findById(original.getId()).orElseThrow().getMessage()).isEqualTo(message);
    }

    @Test void authenticationRolesCsrfAndDisabledAccountsAreEnforced() throws Exception {
        overlap();
        mvc.perform(get("/api/employee-flags")).andExpect(status().isUnauthorized());
        report(manager).andExpect(status().isForbidden());
        mvc.perform(get("/api/employee-flags").with(user(cleaner.getId().toString()))).andExpect(status().isForbidden());
        mvc.perform(post("/api/employee-flags/schedule-overlaps").with(user(cleaner.getId().toString()))
                .contentType("application/json").content(REPORT)).andExpect(status().isForbidden());
        report(cleaner); UUID id = flags.findAll().getFirst().getId();
        mvc.perform(post("/api/employee-flags/{id}/resolve", id).with(user(cleaner.getId().toString())).with(csrf()))
                .andExpect(status().isForbidden());
        cleaner.setActive(false);
        report(cleaner).andExpect(status().isUnauthorized());
    }

    @Test void existingAccountCanSignInReadOwnCalendarReportAndSignOut() throws Exception {
        overlap();
        mvc.perform(post("/api/session/login").with(csrf()).param("username", tenant.getId() + ":" + cleaner.getEmail())
                .param("password", "wrong")).andExpect(status().isUnauthorized());
        var login = mvc.perform(post("/api/session/login").with(csrf()).param("username", tenant.getId() + ":" + cleaner.getEmail())
                .param("password", "test-password")).andExpect(status().isNoContent()).andReturn();
        var session = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(get("/api/session/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(cleaner.getId().toString()));
        mvc.perform(get("/api/me/shifts").session(session).param("fromDate", "2026-11-09").param("toDate", "2026-11-15")
                .param("userId", colleague.getId().toString()).param("tenantId", otherTenant.getId().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.shifts.length()").value(2));
        mvc.perform(get("/api/session/csrf").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
        mvc.perform(post("/api/employee-flags/schedule-overlaps").session(session).with(csrf()).contentType("application/json").content(REPORT))
                .andExpect(status().isOk()).andExpect(jsonPath("$.created").value(1));
        mvc.perform(post("/api/session/logout").session(session).with(csrf())).andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
    }

    @Test void legacyAccountWritesCannotEscalateRolesOrCrossTenants() throws Exception {
        String body = "{\"tenantId\":\"" + tenant.getId() + "\",\"firstName\":\"Alex\",\"lastName\":\"Smith\","
                + "\"email\":\"alex@example.com\",\"role\":\"MANAGER\",\"payRate\":null}";
        mvc.perform(put("/api/cleaner/{id}", cleaner.getId()).with(csrf()).contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/cleaner/{id}", cleaner.getId()).with(user(cleaner.getId().toString())).with(csrf()).contentType("application/json").content(body))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/cleaner/{id}", cleaner.getId()).with(user(outsider.getId().toString())).with(csrf()).contentType("application/json").content(body))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/cleaner/{id}", colleague.getId()).with(user(cleaner.getId().toString())).with(csrf()).contentType("application/json").content(body.replace("MANAGER", "CLEANER")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/cleaner/{id}", cleaner.getId()).with(user(cleaner.getId().toString())).with(csrf()).contentType("application/json").content(body.replace("MANAGER", "CLEANER")))
                .andExpect(status().isNoContent());
        mvc.perform(put("/api/cleaner/{id}", cleaner.getId()).with(user(manager.getId().toString())).with(csrf()).contentType("application/json").content(body))
                .andExpect(status().isNoContent());
        assertThat(cleaner.getRole()).isEqualTo(UserRole.MANAGER);
        String create = "{\"tenantId\":\"" + tenant.getId() + "\",\"firstName\":\"New\",\"lastName\":\"Cleaner\",\"email\":\"new@example.com\",\"passwordHash\":\"temporary-password\"}";
        mvc.perform(post("/api/cleaner").with(csrf()).contentType("application/json").content(create)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/cleaner").with(user(outsider.getId().toString())).with(csrf()).contentType("application/json").content(create)).andExpect(status().isForbidden());
        mvc.perform(post("/api/cleaner").with(user(manager.getId().toString())).with(csrf()).contentType("application/json").content(create)).andExpect(status().isCreated());
        var created = em.createQuery("select u from User u where u.email = 'new@example.com'", User.class).getSingleResult();
        assertThat(new BCryptPasswordEncoder().matches("temporary-password", created.getPasswordHash())).isTrue();
    }

    @Test void realCsrfTokensWorkWithBrowserStyleSessionLoginAndReporting() throws Exception {
        overlap();
        var csrfResponse = mvc.perform(get("/api/session/csrf")).andExpect(status().isOk()).andReturn();
        var session = (MockHttpSession) csrfResponse.getRequest().getSession(false);
        String token = com.jayway.jsonpath.JsonPath.read(csrfResponse.getResponse().getContentAsString(), "$.token");
        mvc.perform(post("/api/session/login").session(session).header("X-CSRF-TOKEN", token)
                .param("username", tenant.getId() + ":" + cleaner.getEmail()).param("password", "test-password"))
                .andExpect(status().isNoContent());
        var next = mvc.perform(get("/api/session/csrf").session(session)).andReturn();
        String fresh = com.jayway.jsonpath.JsonPath.read(next.getResponse().getContentAsString(), "$.token");
        mvc.perform(post("/api/employee-flags/schedule-overlaps").session(session).header("X-CSRF-TOKEN", fresh)
                .contentType("application/json").content(REPORT)).andExpect(status().isOk()).andExpect(jsonPath("$.created").value(1));
    }

    private ResultActions report(User actor) throws Exception {
        return mvc.perform(post("/api/employee-flags/schedule-overlaps").with(user(actor.getId().toString())).with(csrf())
                .contentType("application/json").content(REPORT));
    }
    private void overlap() {
        shift(cleaner, "2026-11-09T09:00", "2026-11-09T12:00");
        shift(cleaner, "2026-11-09T11:00", "2026-11-09T13:00");
    }
    private Tenant tenant(String name) {
        Tenant t = Tenant.builder().name(name).contactName("Pat").contactEmail("pat@example.com")
                .addressLine1("High Street").city("London").postcode("SW1A 1AA").build(); em.persist(t); return t;
    }
    private User createUser(Tenant t, UserRole role) {
        User u = User.builder().tenant(t).firstName("Alex").lastName("Smith").email(UUID.randomUUID() + "@example.com")
                .passwordHash(HASH).role(role).build(); em.persist(u); return u;
    }
    private Site site(Tenant t) {
        Site s = Site.builder().tenant(t).name("Office").addressLine1("High Street").city("London").postcode("SW1A 1AA").build(); em.persist(s); return s;
    }
    private Shift shift(User u, String from, String to) {
        LocalDateTime start = LocalDateTime.parse(from), end = LocalDateTime.parse(to);
        Shift s = Shift.builder().tenant(u.getTenant()).user(u).site(site).shiftDate(start.toLocalDate())
                .scheduledStart(start).scheduledEnd(end).build(); em.persist(s); return s;
    }
}

