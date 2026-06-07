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

-- student_score 表保存学生成绩信息，支持一个学生多门课程、多学期成绩记录。
create table if not exists student_score (
    id bigint primary key auto_increment,
    student_id int not null,
    course_name varchar(100) not null,
    score decimal(5,2) not null,
    semester varchar(30) not null,
    teacher_id int null,
    teacher_name varchar(50) null
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
