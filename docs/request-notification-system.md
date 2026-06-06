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
- 请假 / 调课 / 资料补录等通用审批请求

## 核心角色

- `ADMIN`：查看全部请求、未读数、详情、处理结果
- `USER + STUDENT`：提交学生相关请求，查看自己的请求记录
- `USER + TEACHER`：提交教师相关请求，查看自己的请求记录
- `USER + NONE`：提交通用账号请求，例如“申请绑定身份”

## 业务流程

### 1. 发起请求

前端普通用户在“发起请求”表单中填写：

- 请求类型
- 请求标题
- 请求内容
- 关联对象 ID（可选，例如学生工号、老师工号）

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
    status varchar(20) not null default 'PENDING',
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    handled_by_user_id int null,
    handled_by_username varchar(50) null,
    handled_at datetime null,
    handle_comment varchar(255) null
);
```

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

- `RequestTicket`
- `AdminNotification`
- `RequestTicketLog`

### DTO

- `CreateRequestTicketRequest`
- `HandleRequestTicketRequest`
- `RequestTicketQueryRequest`

### Mapper

- `RequestTicketMapper`
- `AdminNotificationMapper`
- `RequestTicketLogMapper`

### Service

- `RequestTicketService`
- `NotificationService`

### Controller

- `RequestTicketController`
- `AdminNotificationController`

## 接口设计

### 普通用户接口

#### 1. 提交请求

`POST /requests`

请求体：

```json
{
  "requestType": "PROFILE_UPDATE",
  "title": "申请修改学生手机号",
  "content": "我的手机号已变更，请更新为 13800000008",
  "relatedEntityType": "STUDENT",
  "relatedEntityId": 1
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

### 提交请求

- 只要已登录即可提交

### 查看自己的请求

- 只能看自己发起的请求

### 管理员通知

- 只有管理员可以查看铃铛未读数和请求中心

### 请求处理

- 只有管理员可以执行审批动作

## 前端页面建议

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

## 与现有系统的衔接建议

### 1. 账号体系复用

直接复用现有 `user_account` 和 Session 登录态：

- `requester_user_id` 取当前登录用户 ID
- `requester_username` 取当前登录用户名
- `requester_role` 取当前账号角色
- `requester_identity_type` / `requester_identity_id` 取当前绑定信息

### 2. 管理员前端入口

你现在已有 `topbarActions`，很适合直接增加：

- 铃铛按钮
- 未读徽标

### 3. 普通用户入口

可在普通用户面板新增：

- “发起请求”表单
- “我的请求”列表

## 最小落地顺序

建议按这个顺序开发：

1. 建表：`request_ticket`、`admin_notification`
2. 后端：提交请求接口
3. 后端：管理员未读数接口
4. 前端：admin 顶部铃铛 + 数字
5. 后端：管理员请求列表接口
6. 前端：请求中心页
7. 后端：请求详情 + 处理接口
8. 前端：详情页处理动作

## 当前版本的推荐范围

如果你想先做一个教学项目可演示版本，建议第一阶段只做：

- 普通用户提交请求
- 管理员铃铛未读数
- 管理员请求列表
- 管理员查看详情
- 管理员通过 / 驳回

这样链路已经完整，后面再补日志、分页、筛选、消息回推会更轻松。
