create table payment_recovery_settings (
  id smallint primary key check (id = 1),
  activated_at timestamptz not null
);
