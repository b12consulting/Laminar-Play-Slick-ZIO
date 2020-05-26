-- !Ups

create table if not exists users (
    user_name varchar(255),
    hashed_password varchar(255)
);


-- !Downs


drop table if exists users;

