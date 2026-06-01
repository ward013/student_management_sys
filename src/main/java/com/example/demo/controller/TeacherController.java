package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.UserAccount;
import com.example.demo.service.AuthService;
import com.example.demo.service.TeacherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/teachers", produces = "application/json;charset=UTF-8")
public class TeacherController {
    private final TeacherService teacherService;
    private final AuthService authService;

    public TeacherController(TeacherService teacherService, AuthService authService) {
        this.teacherService = teacherService;
        this.authService = authService;
    }

    @GetMapping
    public Result<List<Teacher>> findAll(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success(teacherService.findAll());
    }

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
