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

-- request_ticket 表保存用户发起的请求，例如成绩复核、成绩更正等。
create table if not exists request_ticket (
    id bigint primary key auto_increment,
    requester_user_id int not null,
    requester_username varchar(50) not null,
    requester_role varchar(20) not null,
    requester_identity_type varchar(20) not null,
    requester_identity_id int null,
    request_type varchar(50) not null,
    title varchar(100) not null,
    content text not null,
    related_entity_type varchar(30) null,
    related_entity_id bigint null,
    extra_payload_json text null,
    status varchar(20) not null default 'PENDING',
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    handled_by_user_id int null,
    handled_by_username varchar(50) null,
    handled_at datetime null,
    handle_comment varchar(255) null
);

-- admin_notification 表保存管理员的未读提醒记录。
create table if not exists admin_notification (
    id bigint primary key auto_increment,
    request_id bigint not null,
    receiver_role varchar(20) not null default 'ADMIN',
    is_read tinyint(1) not null default 0,
    created_at datetime not null default current_timestamp,
    read_at datetime null
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
