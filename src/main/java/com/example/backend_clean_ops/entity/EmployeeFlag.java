package com.example.backend_clean_ops.entity;

import com.example.backend_clean_ops.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/** Report-time snapshots deliberately have no foreign keys to mutable schedules/shifts. */
@Entity
@Table(name = "employee_flag", uniqueConstraints = @UniqueConstraint(name = "uq_employee_flag_open",
        columnNames = {"tenant_id", "employee_id", "type", "open_conflict_key"}))
@Getter @Setter @NoArgsConstructor
public class EmployeeFlag extends BaseEntity {
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "employee_name", nullable = false, length = 201) private String employeeName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private EmployeeFlagType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private EmployeeFlagStatus status;
    @Column(nullable = false, columnDefinition = "text") private String message;
    @Column(name = "conflict_key", nullable = false, length = 512) private String conflictKey;
    // NULL for resolved reports: a regular unique constraint permits multiple historical reports.
    @Column(name = "open_conflict_key", length = 512) private String openConflictKey;
    @Column(name = "first_shift_id") private UUID firstShiftId;
    @Column(name = "second_shift_id") private UUID secondShiftId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @Column(name = "resolved_by") private UUID resolvedBy;
}
