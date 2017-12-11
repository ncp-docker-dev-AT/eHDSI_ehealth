create table IF NOT EXISTS property (
  name varchar(255) not null,
  value varchar(255),
  is_smp bit DEFAULT 0,
  primary key (name)
);
