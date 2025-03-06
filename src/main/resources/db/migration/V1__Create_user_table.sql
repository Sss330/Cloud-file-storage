create table users (
  id serial primary key unique,
  login varchar (30) not null,
  password varchar(40) not null
);