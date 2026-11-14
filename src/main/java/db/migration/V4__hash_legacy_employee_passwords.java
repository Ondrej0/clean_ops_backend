package db.migration;

import com.example.backend_clean_ops.service.EmployeePasswords;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** Preserve existing account passwords while replacing legacy plaintext storage with BCrypt. */
public class V4__hash_legacy_employee_passwords extends BaseJavaMigration {
    @Override public void migrate(Context context) throws Exception {
        try (var query = context.getConnection().createStatement();
             var users = query.executeQuery("select id, password_hash from \"user\"");
             var update = context.getConnection().prepareStatement("update \"user\" set password_hash = ? where id = ?")) {
            while (users.next()) {
                String value = users.getString("password_hash");
                if (EmployeePasswords.isBcrypt(value)) continue;
                // Empty legacy credentials stay unusable; no default password is introduced.
                if (value == null || value.isBlank()) continue;
                update.setString(1, EmployeePasswords.hashLegacyValue(value));
                update.setObject(2, users.getObject("id"));
                update.executeUpdate();
            }
        }
    }
}
