insert ignore into student(id, name, age, phone) values
    (1, '张三', 19, '13800000001'),
    (2, '李四', 20, '13800000002'),
    (3, '王五', 21, '13800000003');

insert ignore into teacher(id, name, title, phone) values
    (1001, '陈老师', '辅导员', '13900001001'),
    (1002, '周老师', '讲师', '13900001002');

insert ignore into user_account(id, username, password_hash, role, identity_type, identity_id) values
    (1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'NONE', null);
