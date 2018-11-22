create table IF NOT EXISTS EHNCP_PROPERTY (
  NAME varchar(255) not null,
  VALUE varchar(255),
  IS_SMP bit DEFAULT 0,
  primary key (NAME)
);
