create table users (
  id serial primary key unique,
  login    varchar(64) not null unique,
  password varchar(64) not null
);
