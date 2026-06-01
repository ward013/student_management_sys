package com.example.demo.dto;

import jakarta.validation.constraints.Size;

public class UserUpdateRequest {
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    private String username;

    @Size(min = 6, max = 50, message = "密码长度必须在6到50个字符之间")
    private String password;

    private String role;
    private String identityType;
    private Integer identityId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public Integer getIdentityId() {
        return identityId;
    }

    public void setIdentityId(Integer identityId) {
        this.identityId = identityId;
    }
}
