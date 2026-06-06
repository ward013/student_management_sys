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

// 认证控制器：负责注册、登录、退出登录和身份绑定。
// 给本控制器下的所有接口统一加上 /auth 前缀，并声明 JSON 响应使用 UTF-8，避免中文乱码。
@RestController
@RequestMapping(value = "/auth", produces = "application/json;charset=UTF-8")
public class AuthController {
    // AuthController对应service为AuthService
    private final AuthService authService;
    // AuthController的构造函数,传入AuthService对象
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 注册账号默认为普通用户。使用Result来接收请求的信息，给出返回的统一格式：code,message,data
    // 注册方法中，同时校验参数会烦和请求提
    /* @RequestBody将前端的Json数据转化为RegisterRequest对象，然后将RegisterRequest对象里的参数进行@Valid合法校验
    校验时，根据RegisterRequest类中的@NotBlank、@Size来进行参数校验
     */
    @PostMapping("/register")
    public Result<UserAccount> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    // 登录成功后，后端会把当前用户 id 写入 HttpSession。
    @PostMapping("/login")
    public Result<UserAccount> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return Result.success("登录成功", authService.login(request, session));
    }

    // 查询当前登录用户的信息，前端刷新页面时会先调用这个接口恢复登录态。
    @GetMapping("/me")
    public Result<UserAccount> me(HttpSession session) {
        return Result.success(authService.getCurrentUser(session));
    }

    // 普通用户通过学生工号或老师工号绑定自己的身份信息。
    @PostMapping("/bind")
    public Result<UserAccount> bindIdentity(@Valid @RequestBody BindIdentityRequest request, HttpSession session) {
        return Result.success("身份绑定成功", authService.bindIdentity(session, request));
    }

    // 退出登录，本质上就是让当前 Session 失效。
    @PostMapping("/logout")
    public Result<String> logout(HttpSession session) {
        authService.logout(session);
        return Result.success("退出登录成功", "ok");
    }
}
