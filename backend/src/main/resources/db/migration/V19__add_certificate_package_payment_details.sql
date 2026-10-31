alter table payments add column package_type varchar(32) not null default 'DIGITAL';
alter table payments add column shipping_address varchar(1000);
alter table payments add column selected_certification_slugs varchar(1000);
