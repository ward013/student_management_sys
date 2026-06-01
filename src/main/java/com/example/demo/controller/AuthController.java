package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.BindIdentityRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.UserAccount;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth", produces = "application/json;charset=UTF-8")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<UserAccount> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    public Result<UserAccount> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return Result.success("登录成功", authService.login(request, session));
    }

    @GetMapping("/me")
    public Result<UserAccount> me(HttpSession session) {
        return Result.success(authService.getCurrentUser(session));
    }

    @PostMapping("/bind")
    public Result<UserAccount> bindIdentity(@Valid @RequestBody BindIdentityRequest request, HttpSession session) {
        return Result.success("身份绑定成功", authService.bindIdentity(session, request));
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpSession session) {
        authService.logout(session);
        return Result.success("退出登录成功", "ok");
    }
}
