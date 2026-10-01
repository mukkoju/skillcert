create table payments (
  id uuid primary key default gen_random_uuid(),
  attempt_id uuid not null unique references attempts(id),
  amount_paise integer not null check (amount_paise > 0),
  currency varchar(3) not null default 'INR',
  razorpay_order_id varchar(64) not null unique,
  razorpay_payment_id varchar(64) unique,
  status varchar(24) not null default 'CREATED',
  created_at timestamptz not null default now(),
  paid_at timestamptz
);

create index idx_payments_razorpay_order on payments(razorpay_order_id);
update certifications set price_paise = 19900 where slug = 'cloud-foundations';
