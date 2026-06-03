package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 绑定身份请求体：普通用户提交身份类型和工号来完成账号绑定。
public class BindIdentityRequest {
    @NotBlank(message = "身份类型不能为空")
    private String identityType;

    @NotNull(message = "工号不能为空")
    @Min(value = 1, message = "工号必须大于0")
    private Integer identityId;

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
