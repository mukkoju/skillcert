create extension if not exists pgcrypto;

create table certifications (
  id uuid primary key default gen_random_uuid(),
  slug varchar(80) not null unique,
  title varchar(160) not null,
  description text not null,
  duration_minutes integer not null check (duration_minutes > 0),
  question_count integer not null check (question_count > 0),
  passing_score integer not null check (passing_score > 0),
  price_paise integer not null check (price_paise >= 0),
  published boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table questions (
  id uuid primary key default gen_random_uuid(),
  certification_id uuid not null references certifications(id) on delete cascade,
  prompt text not null,
  theory text,
  position integer not null,
  published boolean not null default false,
  unique(certification_id, position)
);

create table question_options (
  id uuid primary key default gen_random_uuid(),
  question_id uuid not null references questions(id) on delete cascade,
  label text not null,
  position integer not null,
  correct boolean not null default false,
  unique(question_id, position)
);

create table attempts (
  id uuid primary key default gen_random_uuid(),
  certification_id uuid not null references certifications(id),
  recipient_name varchar(160) not null,
  score integer not null,
  total_questions integer not null,
  passed boolean not null,
  created_at timestamptz not null default now()
);

create table attempt_answers (
  id uuid primary key default gen_random_uuid(),
  attempt_id uuid not null references attempts(id) on delete cascade,
  question_id uuid not null references questions(id),
  selected_option_id uuid not null references question_options(id),
  correct boolean not null
);

create index idx_questions_certification on questions(certification_id);
create index idx_options_question on question_options(question_id);
create index idx_attempts_certification on attempts(certification_id);
