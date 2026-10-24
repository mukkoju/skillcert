create table payment_recovery_emails (
  id uuid primary key default gen_random_uuid(),
  attempt_id uuid not null references attempts(id) on delete cascade,
  stage varchar(32) not null,
  resume_token varchar(128) not null unique,
  sent_at timestamptz,
  created_at timestamptz not null default now(),
  unique (attempt_id, stage)
);

create index idx_payment_recovery_emails_attempt on payment_recovery_emails(attempt_id);
