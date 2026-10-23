create table instagram_publications (
  id uuid primary key,
  publication_date date not null unique,
  container_id varchar(128),
  instagram_media_id varchar(128),
  status varchar(32) not null,
  created_at timestamptz not null,
  published_at timestamptz
);
