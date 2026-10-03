alter table attempts add column recipient_email varchar(254);
alter table attempts add column recipient_mobile varchar(40);
alter table issued_certificates add column email_sent_at timestamptz;
