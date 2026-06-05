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
        // 声明UserAccount类型的
        UserAccount userAccount = userAccountMapper.findByUsername(request.getUsername().trim());
        if (userAccount == null || !PasswordUtils.matches(request.getPassword(), userAccount.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        session.setAttribute(SESSION_USER_ID, userAccount.getId());
        return userAccount;
    }

    // 获取当前登录用户，这是大多数受保护接口的第一步。
    public UserAccount getCurrentUser(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (!(userId instanceof Integer)) {
            throw new BusinessException(401, "请先登录");
        }

        UserAccount userAccount = userAccountMapper.findById((Integer) userId);
        if (userAccount == null) {
            session.invalidate();
            throw new BusinessException(401, "登录状态已失效");
        }
        return userAccount;
    }

    // 退出登录，让 Session 失效。
    public void logout(HttpSession session) {
        session.invalidate();
    }

    // 普通用户可以把自己的账号绑定到 student 或 teacher 表中的一条记录。
    public UserAccount bindIdentity(HttpSession session, BindIdentityRequest request) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser.isAdmin()) {
            throw new BusinessException(403, "管理员账号不需要绑定身份");
        }

        String identityType = normalizeIdentityType(request.getIdentityType());
        Integer identityId = request.getIdentityId();
        validateIdentityExists(identityType, identityId);

        UserAccount boundUser = userAccountMapper.findByIdentityBinding(identityType, identityId);
        if (boundUser != null && !boundUser.getId().equals(currentUser.getId())) {
            throw new BusinessException(409, "该身份信息已被其他账号绑定");
        }

        currentUser.setIdentityType(identityType);
        currentUser.setIdentityId(identityId);
        userAccountMapper.updateUser(currentUser);
        return userAccountMapper.findById(currentUser.getId());
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
        if (!"STUDENT".equalsIgnoreCase(currentUser.getIdentityType()) || !studentId.equals(currentUser.getIdentityId())) {
            throw new BusinessException(403, "普通用户只能查看自己的学生信息");
        }
    }

    // 权限守卫：管理员、老师可以查看任意学生；学生只能查看自己的记录。
    public void requireStudentReadable(UserAccount currentUser, Integer studentId) {
        if (currentUser.isAdmin()) {
            return;
        }
        if ("TEACHER".equalsIgnoreCase(currentUser.getIdentityType()) && currentUser.getIdentityId() != null) {
            return;
        }
        requireStudentSelf(currentUser, studentId);
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
        if ("STUDENT".equals(identityType)) {
            if (studentMapper.findById(identityId) == null) {
                throw new BusinessException(404, "学生工号不存在");
            }
            return;
        }

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
        String normalizedType = identityType == null ? "NONE" : identityType.trim().toUpperCase();
        if (!"NONE".equals(normalizedType) && !"STUDENT".equals(normalizedType) && !"TEACHER".equals(normalizedType)) {
            throw new BusinessException(400, "身份类型只能是 NONE、STUDENT 或 TEACHER");
        }
        return normalizedType;
    }
}
