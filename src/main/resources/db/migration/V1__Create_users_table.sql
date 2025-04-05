create table users (
  id serial primary key unique,
  username varchar(64) not null unique,
  password varchar(64) not null
);
