alter table certifications
  add column price_experiment_enabled boolean not null default false,
  add column experiment_price_a_paise integer,
  add column experiment_price_b_paise integer;

alter table attempts
  add column offered_price_paise integer,
  add column price_variant varchar(16);

-- Start every currently published SkillCert course with an even ₹99 / ₹199 test.
-- To return a course to one price, set price_experiment_enabled = false and use price_paise.
update certifications
set price_experiment_enabled = true,
    experiment_price_a_paise = 9900,
    experiment_price_b_paise = 19900
where published = true;
