package com.example.demo.service;

import com.example.demo.common.PasswordUtils;
import com.example.demo.dto.BindIdentityRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.UserAccount;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.mapper.TeacherMapper;
import com.example.demo.mapper.UserAccountMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

// 认证与权限服务：处理登录、注册、身份绑定和角色判断。
//controller-service-mapper-mapper.xml
@Service
public class AuthService {
    // Session 中保存当前登录用户 id 的 key。用于维持当前用户登陆状态信息
    public static final String SESSION_USER_ID = "CURRENT_USER_ID";
    // 三个Mapper映射，分别负责角色所具备的增删改查
    private final UserAccountMapper userAccountMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    // 有参构造函数
    public AuthService(UserAccountMapper userAccountMapper,
                       StudentMapper studentMapper,
                       TeacherMapper teacherMapper) {
        this.userAccountMapper = userAccountMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
    }

    // 注册普通用户。当前项目约定：注册出来的账号统一是 USER 且未绑定身份。
    public UserAccount register(RegisterRequest request) {
        if (userAccountMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException(409, "用户名已存在");
        }
        //使用UserAccount对象userAccount来存储单个账户的信息：
        UserAccount userAccount = new UserAccount();
        // 获取各个属性，姓名、密码hash
        userAccount.setUsername(request.getUsername().trim());//设置姓名
        userAccount.setPasswordHash(PasswordUtils.hash(request.getPassword()));//设置密码的哈希值
        userAccount.setRole("USER");//设置角色信息
        userAccount.setIdentityType("NONE");//设置身份类型为NONE，即默认不绑定身份
        userAccount.setIdentityId(null);//设置身份Id为空，身份type和id都在用户登陆后通过绑定用户的工号来绑定
        userAccountMapper.insertUser(userAccount);
        return userAccountMapper.findByUsername(userAccount.getUsername());
    }

    // 登录：校验用户名密码，成功后把用户 id 写入 Session。
    public UserAccount login(LoginRequest request, HttpSession session) {
        // 声明UserAccount类型的对象userAccount，通过mapper映射调用findByUsername的数据库操作返回UserAccount对象
        UserAccount userAccount = userAccountMapper.findByUsername(request.getUsername().trim());
        //对象userAccount空对象 或 用户输入密码的hash结果与数据库中存储的hash不匹配
        if (userAccount == null || !PasswordUtils.matches(request.getPassword(), userAccount.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        session.setAttribute(SESSION_USER_ID, userAccount.getId());
        System.out.println("登录成功，Session ID = " + session.getId());

        System.out.println("Session 中保存的用户ID = " + session.getAttribute(SESSION_USER_ID));
        return userAccount;
    }

    // 获取当前登录用户，这是大多数受保护接口的第一步。
    public UserAccount getCurrentUser(HttpSession session) {
        // 由login中session设置语句session.setAttribute(SESSION_USER_ID, userAccount.getId());设置的id
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (!(userId instanceof Integer)) {
            //通过这里的判断是否有用户登陆，因为用户在登录时会将这里的session的属性id设置为Integer
            //若未登录，则这里的的、useId为空对象
            throw new BusinessException(401, "请先登录");
        }
        //声明UserAccount对象存储登陆的角色信息
        UserAccount userAccount = userAccountMapper.findById((Integer) userId);
        if (userAccount == null) {//session里有id，但是数据库中无此用户：1️
            //1. 用户账号被删除了
            //2. 数据库数据被清空了
            //3. Session 是旧的
            //4. 用户 id 已经失效
            session.invalidate();//让当前 Session 失效，相当于强制退出登录
            throw new BusinessException(401, "登录状态已失效");//抛出异常值
        }
        return userAccount;//返回用户对象
    }

    // 退出登录，让 Session 失效。
    public void logout(HttpSession session) {
        session.invalidate();
    }

    // 普通用户可以把自己的账号绑定到 student 或 teacher 表中的一条记录。
    public UserAccount bindIdentity(HttpSession session, BindIdentityRequest request) {
        UserAccount currentUser = getCurrentUser(session);//通过getCurrentUser来返回一个用户对象userAccount
        if (currentUser.isAdmin()) {//判断是否管理员
            throw new BusinessException(403, "管理员账号不需要绑定身份");
        }
        // 规范化身份信息
        String identityType = normalizeIdentityType(request.getIdentityType());
        Integer identityId = request.getIdentityId();
        validateIdentityExists(identityType, identityId);//查询该身份的用户是否已经在用户表中绑定了身份（绑定了工号）避免一个工号两个身份
        // 判断用户信息是否被绑定（工号）
        UserAccount boundUser = userAccountMapper.findByIdentityBinding(identityType, identityId);
        if (boundUser != null && !boundUser.getId().equals(currentUser.getId())) {
            throw new BusinessException(409, "该身份信息已被其他账号绑定");
        }
        // 尚未绑定，则设置用户类型和身份id
        currentUser.setIdentityType(identityType);
        currentUser.setIdentityId(identityId);
        userAccountMapper.updateUser(currentUser);//更新用户角色库信息
        return userAccountMapper.findById(currentUser.getId());//返回UserAccount的对象
    }

    // 权限守卫：要求当前用户必须是管理员。
    public void requireAdmin(UserAccount currentUser) {
        if (!currentUser.isAdmin()) {
            throw new BusinessException(403, "只有管理员可以执行此操作");
        }
    }

    // 权限守卫：管理员和已绑定老师身份的普通用户都可以查看学生信息。
    public void requireAdminOrTeacher(UserAccount currentUser) {
        if (currentUser.isAdmin()) {
            return;
        }
        requireBoundTeacher(currentUser);
    }

    // 权限守卫：普通学生用户只能查看自己的 student 记录。
    public void requireStudentSelf(UserAccount currentUser, Integer studentId) {
        if (currentUser.isAdmin()) {
            return;
        }
        // 如果当前角色不为Student或者当前用户id与学生id不匹配
        if (!"STUDENT".equalsIgnoreCase(currentUser.getIdentityType()) || !studentId.equals(currentUser.getIdentityId())) {
            throw new BusinessException(403, "普通用户只能查看自己的学生信息");
        }
    }

    // 权限守卫：管理员、老师可以查看任意学生；学生只能查看自己的记录。
    public void requireStudentReadable(UserAccount currentUser, Integer studentId) {
        if (currentUser.isAdmin()) {//如果是管理员
            return;
        }
        //如果是教师
        if ("TEACHER".equalsIgnoreCase(currentUser.getIdentityType()) && currentUser.getIdentityId() != null) {
            return;
        }
        requireStudentSelf(currentUser, studentId);
    }

    // 成绩读取权限：管理员和老师可查看全部成绩，学生只能查看自己的成绩。
    public void requireScoreReadable(UserAccount currentUser, Integer studentId) {
        requireStudentReadable(currentUser, studentId);
    }

    // 成绩维护权限：管理员和老师可新增、修改成绩。
    public void requireScoreWritable(UserAccount currentUser) {
        requireAdminOrTeacher(currentUser);
    }

    // 权限守卫：要求当前账号已经绑定学生身份。
    public void requireBoundStudent(UserAccount currentUser) {
        if (!"STUDENT".equalsIgnoreCase(currentUser.getIdentityType()) || currentUser.getIdentityId() == null) {
            throw new BusinessException(403, "请先绑定学生身份");
        }
    }

    // 权限守卫：要求当前账号已经绑定老师身份。
    public void requireBoundTeacher(UserAccount currentUser) {
        if (!"TEACHER".equalsIgnoreCase(currentUser.getIdentityType()) || currentUser.getIdentityId() == null) {
            throw new BusinessException(403, "请先绑定老师身份");
        }
    }

    // 校验身份工号是否存在于 student/teacher 表中。
    public void validateIdentityExists(String identityType, Integer identityId) {
        if ("STUDENT".equals(identityType)) {//身份是学生
            if (studentMapper.findById(identityId) == null) {
                throw new BusinessException(404, "学生工号不存在");
            }
            return;
        }
        //如果是身份是教师
        Teacher teacher = teacherMapper.findById(identityId);
        if (teacher == null) {
            throw new BusinessException(404, "老师工号不存在");
        }
    }

    // 统一规范角色输入，避免前端大小写或空值导致逻辑混乱。
    public String normalizeRole(String role) {
        String normalizedRole = role == null ? "USER" : role.trim().toUpperCase();
        if (!"ADMIN".equals(normalizedRole) && !"USER".equals(normalizedRole)) {
            throw new BusinessException(400, "角色只能是 ADMIN 或 USER");
        }
        return normalizedRole;
    }

    // 统一规范身份类型输入。
    public String normalizeIdentityType(String identityType) {
        // 身份类型：空字符串——>NONE;非空则将身份转为大写
        String normalizedType = identityType == null ? "NONE" : identityType.trim().toUpperCase();
        if (!"NONE".equals(normalizedType) && !"STUDENT".equals(normalizedType) && !"TEACHER".equals(normalizedType)) {
            throw new BusinessException(400, "身份类型只能是 NONE、STUDENT 或 TEACHER");
        }
        return normalizedType;
    }
}
