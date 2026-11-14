create table employee_flag (
    id uuid primary key,
    tenant_id uuid not null references "Tenant" (id),
    employee_id uuid not null references "user" (id),
    employee_name varchar(201) not null,
    type varchar(40) not null,
    status varchar(20) not null check (status in ('OPEN', 'RESOLVED')),
    message text not null,
    conflict_key varchar(512) not null,
    open_conflict_key varchar(512),
    first_shift_id uuid,
    second_shift_id uuid,
    created_at timestamp with time zone not null,
    resolved_at timestamp with time zone,
    resolved_by uuid references "user" (id),
    constraint uq_employee_flag_open unique (tenant_id, employee_id, type, open_conflict_key),
    constraint ck_employee_flag_resolution check (
        (status = 'OPEN' and open_conflict_key = conflict_key and open_conflict_key is not null
            and resolved_at is null and resolved_by is null)
        or (status = 'RESOLVED' and open_conflict_key is null and resolved_at is not null and resolved_by is not null)
    )
);
create index ix_employee_flag_tenant_status_created on employee_flag (tenant_id, status, created_at desc);
create index ix_employee_flag_employee_created on employee_flag (tenant_id, employee_id, created_at desc);
