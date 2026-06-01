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

@Service
public class AuthService {
    public static final String SESSION_USER_ID = "CURRENT_USER_ID";

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

    public UserAccount register(RegisterRequest request) {
        if (userAccountMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException(409, "用户名已存在");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(request.getUsername().trim());
        userAccount.setPasswordHash(PasswordUtils.hash(request.getPassword()));
        userAccount.setRole("USER");
        userAccount.setIdentityType("NONE");
        userAccount.setIdentityId(null);
        userAccountMapper.insertUser(userAccount);
        return userAccountMapper.findByUsername(userAccount.getUsername());
    }

    public UserAccount login(LoginRequest request, HttpSession session) {
        UserAccount userAccount = userAccountMapper.findByUsername(request.getUsername().trim());
        if (userAccount == null || !PasswordUtils.matches(request.getPassword(), userAccount.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        session.setAttribute(SESSION_USER_ID, userAccount.getId());
        return userAccount;
    }

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

    public void logout(HttpSession session) {
        session.invalidate();
    }

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

    public void requireAdmin(UserAccount currentUser) {
        if (!currentUser.isAdmin()) {
            throw new BusinessException(403, "只有管理员可以执行此操作");
        }
    }

    public void requireStudentSelf(UserAccount currentUser, Integer studentId) {
        if (currentUser.isAdmin()) {
            return;
        }
        if (!"STUDENT".equalsIgnoreCase(currentUser.getIdentityType()) || !studentId.equals(currentUser.getIdentityId())) {
            throw new BusinessException(403, "普通用户只能查看自己的学生信息");
        }
    }

    public void requireBoundStudent(UserAccount currentUser) {
        if (!"STUDENT".equalsIgnoreCase(currentUser.getIdentityType()) || currentUser.getIdentityId() == null) {
            throw new BusinessException(403, "请先绑定学生身份");
        }
    }

    public void requireBoundTeacher(UserAccount currentUser) {
        if (!"TEACHER".equalsIgnoreCase(currentUser.getIdentityType()) || currentUser.getIdentityId() == null) {
            throw new BusinessException(403, "请先绑定老师身份");
        }
    }

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

    public String normalizeRole(String role) {
        String normalizedRole = role == null ? "USER" : role.trim().toUpperCase();
        if (!"ADMIN".equals(normalizedRole) && !"USER".equals(normalizedRole)) {
            throw new BusinessException(400, "角色只能是 ADMIN 或 USER");
        }
        return normalizedRole;
    }

    public String normalizeIdentityType(String identityType) {
        String normalizedType = identityType == null ? "NONE" : identityType.trim().toUpperCase();
        if (!"NONE".equals(normalizedType) && !"STUDENT".equals(normalizedType) && !"TEACHER".equals(normalizedType)) {
            throw new BusinessException(400, "身份类型只能是 NONE、STUDENT 或 TEACHER");
        }
        return normalizedType;
    }
}
