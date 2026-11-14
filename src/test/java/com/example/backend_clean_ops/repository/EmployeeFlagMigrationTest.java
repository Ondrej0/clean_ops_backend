package com.example.backend_clean_ops.repository;

import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import java.sql.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class EmployeeFlagMigrationTest {
    @Test void migrationsEnforceOpenUniquenessAndPreserveResolvedHistory() throws Exception {
        String url = "jdbc:h2:mem:flags_migration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        try (Connection db = DriverManager.getConnection(url, "sa", "")) {
            // Keep H2's DDL session alive while exercising CHECK expressions created by Flyway.
            var dataSource = new org.springframework.jdbc.datasource.SingleConnectionDataSource(db, true);
            Flyway.configure().dataSource(dataSource).target("3").load().migrate();
            UUID tenant = UUID.randomUUID(), employee = UUID.randomUUID(), manager = UUID.randomUUID();
            try (var insert = db.prepareStatement("""
                    insert into "Tenant" (id, name, contact_name, contact_email, address_line_1, city, postcode,
                    country, subscription_plan, subscription_status, created_at, updated_at)
                    values (?, 'Company', 'Pat', 'pat@example.com', 'Street', 'London', 'SW1A', 'UK', 'starter', 'trial', current_timestamp, current_timestamp)
                    """)) { insert.setObject(1, tenant); insert.executeUpdate(); }
            for (UUID user : new UUID[]{employee, manager}) {
                try (var insert = db.prepareStatement("""
                        insert into "user" (id, tenant_id, first_name, last_name, email, password_hash, role, active, created_at, updated_at)
                        values (?, ?, 'Alex', 'Smith', 'alex@example.com', 'hash', 'CLEANER', true, current_timestamp, current_timestamp)
                        """)) { insert.setObject(1, user); insert.setObject(2, tenant); insert.executeUpdate(); }
            }
            var migrations = Flyway.configure().dataSource(dataSource).load();
            migrations.migrate();
            assertThat(migrations.info().current().getVersion().toString()).isEqualTo("4");
            String stored;
            try (var query = db.createStatement(); var rows = query.executeQuery("select password_hash from \"user\"")) {
                rows.next(); stored = rows.getString(1);
                assertThat(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches("hash", stored)).isTrue();
            }
            migrations.migrate();
            try (var query = db.createStatement(); var rows = query.executeQuery("select password_hash from \"user\"")) {
                rows.next(); assertThat(rows.getString(1)).isEqualTo(stored);
            }
            UUID first = UUID.randomUUID();
            insertFlag(db, first, tenant, employee);
            assertThatThrownBy(() -> insertFlag(db, UUID.randomUUID(), tenant, employee))
                    .isInstanceOf(SQLException.class).extracting(e -> ((SQLException) e).getSQLState()).isEqualTo("23505");
            try (var resolve = db.prepareStatement("update employee_flag set status = 'RESOLVED', open_conflict_key = null, resolved_at = current_timestamp, resolved_by = ? where id = ?")) {
                resolve.setObject(1, manager); resolve.setObject(2, first); assertThat(resolve.executeUpdate()).isOne();
            }
            insertFlag(db, UUID.randomUUID(), tenant, employee);
            try (var query = db.createStatement(); var rows = query.executeQuery("select count(*) from employee_flag")) {
                rows.next(); assertThat(rows.getInt(1)).isEqualTo(2);
            }
            // Status and the nullable uniqueness slot cannot drift apart.
            assertThatThrownBy(() -> { try (var update = db.createStatement()) { update.executeUpdate("update employee_flag set open_conflict_key = null where status = 'OPEN'"); } })
                    .isInstanceOf(SQLException.class);
        }
    }

    private void insertFlag(Connection db, UUID id, UUID tenant, UUID employee) throws SQLException {
        try (var insert = db.prepareStatement("""
                insert into employee_flag (id, tenant_id, employee_id, employee_name, type, status, message,
                    conflict_key, open_conflict_key, first_shift_id, second_shift_id, created_at)
                values (?, ?, ?, 'Alex Smith', 'SCHEDULE_OVERLAP', 'OPEN', 'Saved schedule details',
                    'same-conflict', 'same-conflict', ?, ?, current_timestamp)
                """)) {
            insert.setObject(1, id); insert.setObject(2, tenant); insert.setObject(3, employee);
            insert.setObject(4, UUID.randomUUID()); insert.setObject(5, UUID.randomUUID()); insert.executeUpdate();
        }
    }
}
