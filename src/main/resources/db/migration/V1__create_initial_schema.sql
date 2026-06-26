create table "Tenant" (
    id uuid primary key,
    name varchar(255) not null,
    company_number varchar(50),
    contact_name varchar(255) not null,
    contact_email varchar(255) not null,
    contact_phone varchar(50),
    address_line_1 varchar(255) not null,
    address_line_2 varchar(255),
    city varchar(100) not null,
    postcode varchar(20) not null,
    country varchar(100) not null,
    subscription_plan varchar(50) not null,
    subscription_status varchar(50) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table "user" (
    id uuid primary key,
    tenant_id uuid not null,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    email varchar(255) not null,
    phone varchar(50),
    password_hash varchar(255) not null,
    role varchar(20) not null,
    pay_rate numeric(10, 2),
    active boolean not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_user_tenant
        foreign key (tenant_id) references "Tenant" (id)
);

create table site (
    id uuid primary key,
    tenant_id uuid not null,
    name varchar(255) not null,
    address_line_1 varchar(255) not null,
    address_line_2 varchar(255),
    city varchar(100) not null,
    postcode varchar(20) not null,
    contact_name varchar(255),
    contact_phone varchar(50),
    contact_email varchar(255),
    status varchar(20) not null,
    hourly_rate numeric(10, 2),
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_site_tenant
        foreign key (tenant_id) references "Tenant" (id)
);

create table schedule (
    id uuid primary key,
    tenant_id uuid not null,
    site_id uuid not null,
    name varchar(255) not null,
    active boolean not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_schedule_tenant
        foreign key (tenant_id) references "Tenant" (id),
    constraint fk_schedule_site
        foreign key (site_id) references site (id)
);

create table schedule_rule (
    id uuid primary key,
    tenant_id uuid not null,
    schedule_id uuid not null,
    day_of_week varchar(20) not null,
    start_time time not null,
    end_time time not null,
    active boolean not null,
    created_at timestamp not null,
    constraint fk_schedule_rule_tenant
        foreign key (tenant_id) references "Tenant" (id),
    constraint fk_schedule_rule_schedule
        foreign key (schedule_id) references schedule (id)
);

create table schedule_assignment (
    id uuid primary key,
    tenant_id uuid not null,
    schedule_id uuid not null,
    user_id uuid not null,
    assigned_at timestamp not null,
    created_at timestamp not null,
    constraint schedule_assignment_index_0 unique (schedule_id, user_id),
    constraint fk_schedule_assignment_tenant
        foreign key (tenant_id) references "Tenant" (id),
    constraint fk_schedule_assignment_schedule
        foreign key (schedule_id) references schedule (id),
    constraint fk_schedule_assignment_user
        foreign key (user_id) references "user" (id)
);

create table site_assignment (
    id uuid primary key,
    tenant_id uuid not null,
    site_id uuid not null,
    user_id uuid not null,
    assigned_at timestamp not null,
    created_at timestamp not null,
    constraint fk_site_assignment_tenant
        foreign key (tenant_id) references "Tenant" (id),
    constraint fk_site_assignment_site
        foreign key (site_id) references site (id),
    constraint fk_site_assignment_user
        foreign key (user_id) references "user" (id)
);

create table shift (
    id uuid primary key,
    tenant_id uuid not null,
    site_id uuid not null,
    user_id uuid not null,
    schedule_id uuid,
    shift_date date not null,
    scheduled_start timestamp not null,
    scheduled_end timestamp not null,
    actual_start timestamp,
    actual_end timestamp,
    clock_in_latitude numeric(10, 7),
    clock_in_longitude numeric(10, 7),
    clock_out_latitude numeric(10, 7),
    clock_out_longitude numeric(10, 7),
    status varchar(20) not null,
    manager_note text,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_shift_tenant
        foreign key (tenant_id) references "Tenant" (id),
    constraint fk_shift_site
        foreign key (site_id) references site (id),
    constraint fk_shift_user
        foreign key (user_id) references "user" (id),
    constraint fk_shift_schedule
        foreign key (schedule_id) references schedule (id)
);

create table issue (
    id uuid primary key,
    tenant_id uuid not null,
    site_id uuid not null,
    created_by_user_id uuid not null,
    title varchar(255) not null,
    description text,
    severity varchar(20) not null,
    status varchar(20) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_issue_tenant
        foreign key (tenant_id) references "Tenant" (id),
    constraint fk_issue_site
        foreign key (site_id) references site (id),
    constraint fk_issue_created_by_user
        foreign key (created_by_user_id) references "user" (id)
);

create table site_note (
    id uuid primary key,
    tenant_id uuid not null,
    site_id uuid not null,
    created_by_user_id uuid not null,
    note text not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_site_note_tenant
        foreign key (tenant_id) references "Tenant" (id),
    constraint fk_site_note_site
        foreign key (site_id) references site (id),
    constraint fk_site_note_created_by_user
        foreign key (created_by_user_id) references "user" (id)
);

create table checklist (
    id uuid primary key,
    tenant_id uuid not null,
    site_id uuid not null,
    name varchar(255) not null,
    active boolean not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_checklist_tenant
        foreign key (tenant_id) references "Tenant" (id),
    constraint fk_checklist_site
        foreign key (site_id) references site (id)
);

create table checklist_item (
    id uuid primary key,
    tenant_id uuid not null,
    checklist_id uuid not null,
    description varchar(500) not null,
    sort_order integer not null,
    mandatory boolean not null,
    created_at timestamp not null,
    constraint fk_checklist_item_tenant
        foreign key (tenant_id) references "Tenant" (id),
    constraint fk_checklist_item_checklist
        foreign key (checklist_id) references checklist (id)
);
