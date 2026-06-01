create table if not exists student (
    id int primary key,
    name varchar(50) not null,
    age int not null,
    phone varchar(20) not null
);

create table if not exists teacher (
    id int primary key,
    name varchar(50) not null,
    title varchar(50) not null,
    phone varchar(20) not null
);

create table if not exists user_account (
    id int primary key auto_increment,
    username varchar(50) not null unique,
    password_hash varchar(64) not null,
    role varchar(20) not null,
    identity_type varchar(20) not null,
    identity_id int null
);
