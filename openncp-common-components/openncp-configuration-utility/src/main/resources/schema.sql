create table IF NOT EXISTS property (
  name varchar(255) not null,
  value varchar(255),
  is_smp bit,
  primary key (name)
);
