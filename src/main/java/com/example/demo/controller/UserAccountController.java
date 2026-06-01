package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.entity.UserAccount;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserAccountService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/users", produces = "application/json;charset=UTF-8")
public class UserAccountController {
    private final UserAccountService userAccountService;
    private final AuthService authService;

    public UserAccountController(UserAccountService userAccountService, AuthService authService) {
        this.userAccountService = userAccountService;
        this.authService = authService;
    }

    @GetMapping
    public Result<List<UserAccount>> findAll(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success(userAccountService.findAll());
    }

    @PutMapping("/{id}")
    public Result<UserAccount> updateUser(@PathVariable Integer id,
                                          @Valid @RequestBody UserUpdateRequest request,
                                          HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success("用户信息更新成功", userAccountService.updateUser(id, request));
    }
}
