package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.UserAccount;
import com.example.demo.service.AuthService;
import com.example.demo.service.TeacherService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 老师信息控制器：管理员可以查看全部老师，普通老师用户只能查看自己的资料。
@RestController
@RequestMapping(value = "/teachers", produces = "application/json;charset=UTF-8")
public class TeacherController {
    private final TeacherService teacherService;
    private final AuthService authService;

    public TeacherController(TeacherService teacherService, AuthService authService) {
        this.teacherService = teacherService;
        this.authService = authService;
    }
    // 添加老师信息：工号、姓名、职称、手机
    @PostMapping
    public Result<Teacher> insertTeacher(@Valid @RequestBody Teacher teacher, HttpSession session) {
        UserAccount currentUser =authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success("添加老师信息成功",teacherService.addTeacher(teacher));

    }

    // 管理员查看全部老师信息。
    @GetMapping
    public Result<List<Teacher>> findAll(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success(teacherService.findAll());
    }

    // 已绑定老师身份的普通用户查看自己的老师资料。
    @GetMapping("/me")
    public Result<Teacher> findCurrentTeacher(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        if (currentUser.isAdmin()) {
            throw new com.example.demo.exception.BusinessException(403, "管理员请使用老师列表接口");
        }
        authService.requireBoundTeacher(currentUser);
        return Result.success(teacherService.findById(currentUser.getIdentityId()));
    }
}
