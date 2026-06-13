package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 管理员处理请求时的请求体：包含审批状态和处理意见。
public class HandleRequestTicketRequest {
    @NotBlank(message = "处理状态不能为空")
    @Size(max = 20, message = "处理状态长度不能超过20个字符")
    private String status;

    @Size(max = 255, message = "处理意见长度不能超过255个字符")
    private String handleComment;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHandleComment() {
        return handleComment;
    }

    public void setHandleComment(String handleComment) {
        this.handleComment = handleComment;
    }
}
