create table attempt_audits (
  id uuid primary key default gen_random_uuid(),
  attempt_id uuid not null unique references attempts(id) on delete cascade,
  source varchar(64),
  utm_source varchar(160),
  utm_medium varchar(160),
  utm_campaign varchar(255),
  utm_content varchar(255),
  utm_term varchar(255),
  referrer text,
  browser varchar(80),
  operating_system varchar(80),
  device_type varchar(32),
  in_app_browser boolean,
  country varchar(80),
  state varchar(120),
  city varchar(120),
  timezone varchar(80),
  viewport_width integer,
  landing_at timestamptz,
  assessment_started_at timestamptz,
  assessment_completed_at timestamptz,
  created_at timestamptz not null default now()
);

create index idx_attempt_audits_source on attempt_audits(source);
create index idx_attempt_audits_utm_campaign on attempt_audits(utm_campaign);
