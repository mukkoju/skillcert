alter table payment_recovery_emails
  add column delivery_attempts integer not null default 0,
  add column failed_at timestamptz;
