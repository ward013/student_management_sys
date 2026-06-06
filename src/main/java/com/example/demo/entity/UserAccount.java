package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

// UserAccount 实体对应 user_account 表，保存登录账号、角色和绑定身份信息。
public class UserAccount {
    private Integer id;
    private String username;

    // 密码摘要不应该返回给前端，所以这里用 @JsonIgnore 隐藏。
    @JsonIgnore
    private String passwordHash;

    private String role;
    private String identityType;
    private Integer identityId;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
    // 便捷方法：快速判断当前账号是不是管理员。
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
