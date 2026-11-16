alter table shift add column source_user_id uuid;
alter table shift add column source_scheduled_start timestamp;
alter table shift add column source_scheduled_end timestamp;
alter table shift add column manual_override boolean not null default false;
alter table shift add column removed_at timestamp;

update shift
set source_user_id = user_id,
    source_scheduled_start = scheduled_start,
    source_scheduled_end = scheduled_end
where schedule_id is not null;

create index idx_shift_override_origin
    on shift (schedule_id, source_user_id, source_scheduled_start, source_scheduled_end, manual_override);
create index idx_shift_removed_at on shift (removed_at);
