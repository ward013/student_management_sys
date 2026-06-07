# 请求通知系统设计

## 目标

为学生/教师/普通用户提供一套“发起请求 -> 后台受理 -> 管理员处理 -> 铃铛提醒”的完整流程。

本方案重点解决四件事：

1. 普通角色可以提交业务请求。
2. 管理员在顶部铃铛看到未处理消息条数。
3. 管理员点击铃铛进入详情页查看请求清单。
4. 管理员可以查看请求详情，包括时间、发起人、请求内容、处理状态。

## 适用场景

- 账号权限申请
- 身份绑定申请
- 学生信息修改申请
- 教师信息修改申请
- 学生成绩查询
- 学生成绩修改申请
- 请假 / 调课 / 资料补录等通用审批请求

## 核心角色

- `ADMIN`：查看全部请求、未读数、详情、处理结果
- `USER + STUDENT`：提交学生相关请求，查看自己的请求记录和自己的成绩
- `USER + TEACHER`：提交教师相关请求，查看自己的请求记录、查看学生信息和学生成绩
- `USER + NONE`：提交通用账号请求，例如“申请绑定身份”

## 业务流程

### 1. 发起请求

前端普通用户在“发起请求”表单中填写：

- 请求类型
- 请求标题
- 请求内容
- 关联对象 ID（可选，例如学生工号、老师工号）

如果是成绩相关请求，建议再补充：

- 课程名称
- 原成绩
- 目标成绩
- 修改原因

提交后，后端创建一条请求记录，状态为 `PENDING`。

### 2. 生成管理员通知

请求创建成功后，同时生成一条管理员通知，状态为 `UNREAD`。

管理员顶部铃铛读取所有 `UNREAD` 通知数量，显示红点或数字徽标。

### 3. 管理员查看铃铛

管理员点击铃铛，进入“请求中心”页面，看到请求列表：

- 发起时间
- 发起人用户名
- 发起人角色 / 身份
- 请求类型
- 请求标题
- 当前状态
- 是否已读

### 4. 管理员查看详情

管理员点击列表项后，进入详情页或右侧详情抽屉，查看：

- 请求 ID
- 发起时间
- 发起人用户名
- 发起人角色
- 身份类型
- 关联工号
- 请求完整内容
- 附加备注
- 当前审批状态
- 处理人
- 处理时间
- 处理意见

### 5. 管理员处理请求

管理员可执行：

- 通过 `APPROVED`
- 驳回 `REJECTED`
- 标记处理中 `PROCESSING`

处理完成后，请求主表状态更新，通知状态也同步更新。

### 6. 反馈给发起人

后续可扩展“我的消息”页面，让发起人看到：

- 请求是否已处理
- 管理员处理意见
- 处理时间

## 数据库设计

### 0. 学生成绩表 `student_score`

如果系统要支持成绩查询和成绩维护，建议单独增加成绩表，而不是把成绩直接塞到 `student` 表里。

```sql
create table if not exists student_score (
    id bigint primary key auto_increment,
    student_id int not null,
    course_name varchar(100) not null,
    score decimal(5,2) not null,
    semester varchar(30) not null,
    teacher_id int null,
    teacher_name varchar(50) null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp
);
```

说明：

- 一个学生可以有多条成绩记录
- 一门课程在不同学期可以有不同成绩
- `teacher_id` / `teacher_name` 用于标记成绩的录入或维护教师
- 如果后续要支持更细的授课关系，可再拆课程表和选课表

### 1. 请求主表 `request_ticket`

```sql
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
    related_entity_id int null,
    extra_payload_json text null,
    status varchar(20) not null default 'PENDING',
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    handled_by_user_id int null,
    handled_by_username varchar(50) null,
    handled_at datetime null,
    handle_comment varchar(255) null
);
```

建议：

- `related_entity_type` 可支持 `STUDENT`、`TEACHER`、`STUDENT_SCORE`
- `related_entity_id` 在成绩场景下可存 `student_score.id`
- `extra_payload_json` 用来存成绩修改前后值，例如课程名、原成绩、目标成绩、学期等结构化信息

### 2. 管理员通知表 `admin_notification`

```sql
create table if not exists admin_notification (
    id bigint primary key auto_increment,
    request_id bigint not null,
    receiver_role varchar(20) not null default 'ADMIN',
    is_read tinyint(1) not null default 0,
    created_at datetime not null default current_timestamp,
    read_at datetime null,
    foreign key (request_id) references request_ticket(id)
);
```

### 3. 可选的请求流转日志表 `request_ticket_log`

如果你希望完整追踪每次处理动作，建议再加：

```sql
create table if not exists request_ticket_log (
    id bigint primary key auto_increment,
    request_id bigint not null,
    action varchar(30) not null,
    operator_user_id int not null,
    operator_username varchar(50) not null,
    comment varchar(255) null,
    created_at datetime not null default current_timestamp,
    foreign key (request_id) references request_ticket(id)
);
```

## 后端分层建议

### Entity

- `StudentScore`
- `RequestTicket`
- `AdminNotification`
- `RequestTicketLog`

### DTO

- `CreateStudentScoreRequest`
- `UpdateStudentScoreRequest`
- `StudentScoreQueryRequest`
- `CreateRequestTicketRequest`
- `HandleRequestTicketRequest`
- `RequestTicketQueryRequest`

### Mapper

- `StudentScoreMapper`
- `RequestTicketMapper`
- `AdminNotificationMapper`
- `RequestTicketLogMapper`

### Service

- `StudentScoreService`
- `RequestTicketService`
- `NotificationService`

### Controller

- `StudentScoreController`
- `RequestTicketController`
- `AdminNotificationController`

## 接口设计

### 学生成绩接口

#### 1. 查询学生成绩列表

`GET /scores`

支持条件：

- `studentId`
- `courseName`
- `semester`

建议行为：

- 管理员：可看全部成绩
- 教师：可按学生、课程、学期查看成绩
- 学生：只返回自己的成绩

#### 2. 查询单条成绩详情

`GET /scores/{id}`

#### 3. 新增成绩

`POST /scores`

请求体：

```json
{
  "studentId": 1,
  "courseName": "高等数学",
  "score": 92,
  "semester": "2026-春",
  "teacherId": 1001
}
```

#### 4. 修改成绩

`PUT /scores/{id}`

请求体：

```json
{
  "score": 95,
  "semester": "2026-春"
}
```

#### 5. 学生查看自己的成绩

`GET /scores/me`

返回当前登录学生绑定工号对应的全部成绩。

### 普通用户接口

#### 1. 提交请求

`POST /requests`

请求体：

```json
{
  "requestType": "GRADE_UPDATE",
  "title": "申请修改高等数学成绩",
  "content": "期末复核后成绩应从 88 调整为 92，请管理员审核",
  "relatedEntityType": "STUDENT_SCORE",
  "relatedEntityId": 15,
  "extraPayloadJson": {
    "studentId": 1,
    "courseName": "高等数学",
    "semester": "2026-春",
    "oldScore": 88,
    "newScore": 92,
    "reason": "复核卷面后更新"
  }
}
```

#### 2. 查看我的请求

`GET /requests/my`

#### 3. 查看我的单条请求详情

`GET /requests/my/{id}`

### 管理员接口

#### 1. 查询未读通知数

`GET /admin/notifications/unread-count`

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": 5
}
```

#### 2. 查询通知列表

`GET /admin/notifications`

支持条件：

- `readStatus`
- `status`
- `requestType`
- `page`
- `size`

#### 3. 标记通知已读

`PUT /admin/notifications/{id}/read`

#### 4. 查看请求详情

`GET /admin/requests/{id}`

#### 5. 处理请求

`PUT /admin/requests/{id}/handle`

请求体：

```json
{
  "status": "APPROVED",
  "handleComment": "已核对资料，允许修改"
}
```

## 权限规则

### 成绩查询权限

- `ADMIN`：可查询全部学生成绩
- `USER + TEACHER`：可查询学生成绩
- `USER + STUDENT`：只能查询自己的成绩
- `USER + NONE`：不能查询成绩

### 成绩修改权限

- `ADMIN`：可直接新增、修改成绩
- `USER + TEACHER`：建议分两种实现方式
  - 教学演示简化版：允许教师直接修改成绩
  - 审批流正式版：教师提交成绩修改请求，由管理员审批后生效
- `USER + STUDENT`：不能直接修改成绩，只能发起成绩复核/修改申请

### 成绩相关请求权限

- 学生可发起“成绩复核”或“成绩异议”请求
- 教师可发起“成绩更正”请求
- 管理员可查看、审批、驳回所有成绩请求

### 提交请求

- 只要已登录即可提交

### 查看自己的请求

- 只能看自己发起的请求

### 管理员通知

- 只有管理员可以查看铃铛未读数和请求中心

### 请求处理

- 只有管理员可以执行审批动作

## 前端页面建议

### 0. 学生成绩模块

建议把成绩功能做成独立页面或独立卡片区域。

#### 管理员视图

增加“学生成绩表”区域，支持：

- 成绩列表
- 按学生姓名 / 工号筛选
- 按课程筛选
- 按学期筛选
- 新增成绩
- 修改成绩

#### 教师视图

在教师资料页下方增加“学生成绩”模块，支持：

- 查看学生成绩列表
- 按课程 / 学期筛选
- 录入或修改成绩
- 发起成绩更正请求

#### 学生视图

在学生个人资料页下方增加“我的成绩”模块，显示：

- 课程名
- 学期
- 分数
- 任课教师
- 平均分 / 总评（可选）

并提供一个按钮：

- “申请成绩复核”

### 1. 顶部铃铛

放在管理员顶部栏，显示：

- 铃铛图标
- 未读数徽标

行为：

- 页面初始化时调用 `/admin/notifications/unread-count`
- 管理员每次处理请求后刷新未读数
- 点击铃铛跳转 `/admin/requests-center`

### 2. 请求中心页

列表字段建议：

- 未读标记
- 请求标题
- 发起人
- 请求类型
- 发起时间
- 当前状态

支持：

- 按状态筛选
- 按请求类型筛选
- 按未读/已读筛选
- 按是否为成绩请求筛选

### 3. 请求详情页或抽屉

分成三块：

1. 基本信息
2. 请求正文
3. 处理区

处理区提供：

- 通过按钮
- 驳回按钮
- 处理中按钮
- 备注输入框

如果是成绩请求，详情区建议额外显示：

- 学生姓名 / 工号
- 课程名称
- 学期
- 原成绩
- 目标成绩
- 申请原因

## 状态枚举建议

### 请求状态 `request_ticket.status`

- `PENDING`
- `PROCESSING`
- `APPROVED`
- `REJECTED`
- `CANCELLED`

### 通知已读状态

- `0` 未读
- `1` 已读

### 请求类型 `request_ticket.request_type`

- `PROFILE_UPDATE`
- `IDENTITY_BIND`
- `GRADE_REVIEW`
- `GRADE_UPDATE`
- `GENERAL_APPLICATION`

## 与现有系统的衔接建议

### 1. 账号体系复用

直接复用现有 `user_account` 和 Session 登录态：

- `requester_user_id` 取当前登录用户 ID
- `requester_username` 取当前登录用户名
- `requester_role` 取当前账号角色
- `requester_identity_type` / `requester_identity_id` 取当前绑定信息

### 1.1 成绩体系建议

可新增一组与现有 `student`、`teacher` 并列的成绩模块：

- `student_score`
- `StudentScoreMapper`
- `StudentScoreService`
- `StudentScoreController`

查询关系建议：

- 学生通过 `identityId -> student.id -> student_score.student_id`
- 教师通过教师身份查看成绩列表
- 管理员直接查看全部成绩

### 2. 管理员前端入口

你现在已有 `topbarActions`，很适合直接增加：

- 铃铛按钮
- 未读徽标

### 3. 普通用户入口

可在普通用户面板新增：

- “发起请求”表单
- “我的请求”列表
- “我的成绩”区域（学生）
- “学生成绩”区域（教师）

## 最小落地顺序

建议按这个顺序开发：

1. 建表：`student_score`
2. 后端：成绩查询接口、成绩修改接口
3. 前端：管理员 / 教师 / 学生成绩展示区域
4. 建表：`request_ticket`、`admin_notification`
5. 后端：提交请求接口
6. 后端：管理员未读数接口
7. 前端：admin 顶部铃铛 + 数字
8. 后端：管理员请求列表接口
9. 前端：请求中心页
10. 后端：请求详情 + 处理接口
11. 前端：详情页处理动作

## 当前版本的推荐范围

如果你想先做一个教学项目可演示版本，建议第一阶段只做：

- 学生成绩表与成绩查询
- 管理员修改成绩
- 学生查看自己的成绩
- 教师查看学生成绩
- 普通用户提交请求
- 管理员铃铛未读数
- 管理员请求列表
- 管理员查看详情
- 管理员通过 / 驳回

这样链路已经完整，后面再补日志、分页、筛选、消息回推会更轻松。
