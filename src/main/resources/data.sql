-- 初始化学生数据，方便普通用户绑定学生身份并测试“只能看自己”的权限逻辑。
insert ignore into student(id, name, age, phone) values
    (1, '张三', 19, '13800000001'),
    (2, '李四', 20, '13800000002'),
    (3, '王五', 21, '13800000003');

-- 初始化老师数据，方便普通用户绑定老师身份。
insert ignore into teacher(id, name, title, phone) values
    (1001, '陈老师', '辅导员', '13900001001'),
    (1002, '周老师', '讲师', '13900001002');

-- 初始化学生成绩数据，方便管理员、教师、学生演示成绩查询与维护。
insert ignore into student_score(id, student_id, course_name, score, semester, teacher_id, teacher_name) values
    (1, 1, '高等数学', 92.00, '2026-春', 1001, '陈老师'),
    (2, 1, '大学英语', 88.50, '2026-春', 1002, '周老师'),
    (3, 2, '高等数学', 81.00, '2026-春', 1001, '陈老师'),
    (4, 2, '程序设计基础', 90.00, '2026-春', 1002, '周老师'),
    (5, 3, '大学物理', 85.50, '2026-春', 1001, '陈老师');

-- 初始化管理员账号：admin / admin123。
insert ignore into user_account(id, username, password_hash, role, identity_type, identity_id) values
    (1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'NONE', null);
