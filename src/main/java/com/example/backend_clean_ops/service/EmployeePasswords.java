package com.example.backend_clean_ops.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** The legacy passwordHash request field also carries temporary plaintext passwords. */
public final class EmployeePasswords {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private EmployeePasswords() {}
    public static boolean isBcrypt(String value) {
        return value != null && value.matches("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}");
    }
    public static String hashLegacyValue(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("A temporary password is required.");
        return isBcrypt(value) ? value : ENCODER.encode(value);
    }
}
