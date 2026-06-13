package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 提交请求时的请求体：普通用户通过它传入类型、标题、内容和关联对象。
public class CreateRequestTicketRequest {
    @NotBlank(message = "请求类型不能为空")
    @Size(max = 50, message = "请求类型长度不能超过50个字符")
    private String requestType;

    @NotBlank(message = "请求标题不能为空")
    @Size(max = 100, message = "请求标题长度不能超过100个字符")
    private String title;

    @NotBlank(message = "请求内容不能为空")
    private String content;

    @Size(max = 30, message = "关联类型长度不能超过30个字符")
    private String relatedEntityType;

    // relatedEntityId 在成绩请求里一般对应 student_score 表中的成绩记录 id。
    private Long relatedEntityId;

    // extraPayloadJson 允许前端把更多结构化信息一起提交给后端。
    private String extraPayloadJson;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getExtraPayloadJson() {
        return extraPayloadJson;
    }

    public void setExtraPayloadJson(String extraPayloadJson) {
        this.extraPayloadJson = extraPayloadJson;
    }
}
