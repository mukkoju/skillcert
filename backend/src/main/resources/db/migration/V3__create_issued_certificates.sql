create table issued_certificates (
  id uuid primary key default gen_random_uuid(),
  attempt_id uuid not null unique references attempts(id),
  certification_id uuid not null references certifications(id),
  recipient_name varchar(160) not null,
  short_id varchar(24) not null unique,
  score integer not null,
  total_questions integer not null,
  status varchar(24) not null default 'ISSUED',
  issued_at timestamptz not null default now()
);

create index idx_issued_certificates_short_id on issued_certificates(short_id);
create index idx_issued_certificates_certification on issued_certificates(certification_id);
