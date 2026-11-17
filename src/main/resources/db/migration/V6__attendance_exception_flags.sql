-- Attendance exceptions reuse employee_flag's immutable snapshot and open-key lifecycle.
create index ix_employee_flag_attendance_shift on employee_flag (tenant_id, type, first_shift_id);
