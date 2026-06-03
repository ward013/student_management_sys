-- student 表保存学生基础资料，同时也是普通学生账号绑定身份时的目标表。
create table if not exists student (
    id int primary key,
    name varchar(50) not null,
    age int not null,
    phone varchar(20) not null
);

-- teacher 表保存老师基础资料。
create table if not exists teacher (
    id int primary key,
    name varchar(50) not null,
    title varchar(50) not null,
    phone varchar(20) not null
);

-- user_account 表保存登录账号、角色和身份绑定关系。
create table if not exists user_account (
    id int primary key auto_increment,
    username varchar(50) not null unique,
    password_hash varchar(64) not null,
    role varchar(20) not null,
    identity_type varchar(20) not null,
    identity_id int null
);
