# 项目结构与功能说明

## 一、项目定位

本项目是一个基于 `Spring Boot + MyBatis + MySQL + 原生前端页面` 的学生管理系统教学项目，覆盖了：

- 用户注册、登录、退出
- 身份绑定
- 学生、教师基础资料管理
- 学生成绩查询与维护
- 成绩请求提交
- 管理员铃铛消息中心与审批处理

项目既适合课程实验，也适合作为权限控制、Session 登录态、前后端协作的练习样例。

## 二、目录结构

### 1. 后端源码

`src/main/java/com/example/demo`

- `common`
  - 通用返回体与工具类
  - 例如 `Result`
- `controller`
  - 接口控制器
  - 负责接收请求、参数校验、调用 Service
- `dto`
  - 请求参数对象
  - 例如登录、绑定身份、提交请求、处理请求
- `entity`
  - 数据实体类
  - 对应数据库表结构
- `exception`
  - 业务异常与统一异常处理
- `mapper`
  - MyBatis Mapper 接口
- `service`
  - 业务逻辑层
  - 权限判断、数据校验、流程编排

### 2. 资源文件

`src/main/resources`

- `mapper`
  - MyBatis XML SQL 映射文件
- `static`
  - 前端页面静态资源
  - `index.html`
  - `app.css`
  - `app.js`
- `schema.sql`
  - 数据库建表脚本
- `data.sql`
  - 初始化测试数据
- `application.properties`
  - 端口、数据源、MyBatis 等配置

### 3. 文档与展示资源

- `README.md`
  - 项目说明与运行指南
- `docs/request-notification-system.md`
  - 请求通知与成绩模块设计说明
- `docs/project-structure-and-features.md`
  - 当前这份项目结构与功能文档
- `README-assets`
  - README 中使用的前端页面预览图与预览 HTML

## 三、数据库表结构

当前版本核心表包括：

- `student`
  - 学生基础资料
- `teacher`
  - 教师基础资料
- `student_score`
  - 学生成绩信息
- `user_account`
  - 登录账号、角色、身份绑定关系
- `request_ticket`
  - 用户提交的请求记录
- `admin_notification`
  - 管理员未读通知

## 四、功能模块

### 1. 认证与登录

支持：

- 普通用户注册
- 用户登录
- Session 登录态恢复
- 退出登录

关键接口：

- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/me`
- `POST /auth/logout`

### 2. 身份绑定

普通用户可将账号绑定到：

- 学生身份
- 教师身份

关键接口：

- `POST /auth/bind`

规则：

- 一个身份记录只能绑定一个账号
- 管理员不需要绑定身份

### 3. 学生资料管理

管理员可：

- 查询全部学生
- 查询单个学生
- 新增学生
- 修改学生
- 删除学生

教师可：

- 查询学生列表
- 查询学生详情

学生可：

- 查看自己的学生资料

关键接口：

- `GET /students`
- `GET /students/{id}`
- `GET /students/me`
- `POST /students`
- `PUT /students/{id}`
- `DELETE /students/{id}`

### 4. 教师资料管理

管理员可：

- 查询全部教师
- 查询单个教师
- 新增教师
- 修改教师

教师可：

- 查看自己的教师资料

关键接口：

- `GET /teachers`
- `GET /teachers/{id}`
- `GET /teachers/me`
- `POST /teachers`
- `PUT /teachers/{id}`

### 5. 成绩管理

管理员可：

- 查看全部成绩
- 新增成绩
- 修改成绩

教师可：

- 查看学生成绩
- 新增成绩
- 修改成绩

学生可：

- 查看自己的成绩

关键接口：

- `GET /scores`
- `GET /scores/{id}`
- `GET /scores/me`
- `POST /scores`
- `PUT /scores/{id}`

### 6. 请求提交与管理员铃铛

普通用户可提交：

- 成绩复核请求
- 成绩更正请求
- 通用申请

管理员可：

- 查看顶部铃铛未读数
- 查看请求列表
- 查看请求详情
- 标记请求已读
- 审批通过 / 驳回 / 标记处理中

关键接口：

- `POST /requests`
- `GET /requests/my`
- `GET /requests/my/{id}`
- `GET /admin/notifications/unread-count`
- `GET /admin/requests`
- `GET /admin/requests/{id}`
- `PUT /admin/requests/{id}/read`
- `PUT /admin/requests/{id}/handle`

## 五、权限模型

### 1. ADMIN

可访问：

- 全部用户
- 全部学生
- 全部教师
- 全部成绩
- 全部请求
- 通知铃铛与审批中心

### 2. USER + STUDENT

可访问：

- 自己的学生资料
- 自己的成绩
- 自己提交的请求
- 提交成绩复核请求

### 3. USER + TEACHER

可访问：

- 自己的教师资料
- 学生列表
- 学生成绩
- 新增 / 修改成绩
- 自己提交的请求
- 提交成绩更正请求

### 4. USER + NONE

可访问：

- 基础账号信息
- 绑定身份入口
- 通用请求提交

## 六、前端页面结构

### 1. 登录 / 注册页

功能：

- 登录
- 注册
- 管理员快捷填充
- 示例工号展示

### 2. 管理员控制台

功能：

- 顶部状态栏
- 铃铛消息中心
- 用户列表与用户编辑
- 学生列表与学生维护
- 教师列表与教师维护
- 成绩列表与成绩维护

### 3. 教师视图

功能：

- 教师个人资料
- 学生列表
- 学生成绩列表
- 成绩维护表单
- 请求提交与我的请求

### 4. 学生视图

功能：

- 学生个人资料
- 我的成绩
- 请求提交与我的请求

## 七、当前版本建议演示流程

建议演示顺序：

1. 管理员登录，查看全局数据
2. 普通用户绑定学生身份
3. 学生登录查看自己的成绩
4. 学生发起成绩复核请求
5. 管理员顶部铃铛显示未读数
6. 管理员打开请求中心，查看详情并处理
7. 教师登录查看学生成绩并修改成绩

## 八、后续可扩展方向

- 请求筛选、分页、搜索
- 请求流转日志
- 课程表、授课关系、选课表
- 更细粒度的教师授课权限
- 铃铛实时轮询或 WebSocket 推送
- 图表化成绩统计
