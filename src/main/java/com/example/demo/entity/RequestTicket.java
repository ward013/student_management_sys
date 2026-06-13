package com.example.demo.entity;

import java.time.LocalDateTime;

// RequestTicket 保存用户发起的业务请求，例如成绩复核、成绩更正。
public class RequestTicket {
    private Long id;
    // requester* 这一组字段用于快照保存发起请求时的用户信息。
    private Integer requesterUserId;
    private String requesterUsername;
    private String requesterRole;
    private String requesterIdentityType;
    private Integer requesterIdentityId;
    // relatedEntity* 用于关联学生、教师或成绩记录。
    private String requestType;
    private String title;
    private String content;
    private String relatedEntityType;
    private Long relatedEntityId;
    // extraPayloadJson 用来保存额外结构化信息，例如原成绩、目标成绩等。
    private String extraPayloadJson;
    // status 表示当前审批状态，例如 PENDING、APPROVED。
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // handled* 这一组字段保存管理员审批后的处理信息。
    private Integer handledByUserId;
    private String handledByUsername;
    private LocalDateTime handledAt;
    private String handleComment;
    // read 不是 request_ticket 表本身的字段，而是联表查管理员通知时带回来的已读状态。
    private Boolean read;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRequesterUserId() {
        return requesterUserId;
    }

    public void setRequesterUserId(Integer requesterUserId) {
        this.requesterUserId = requesterUserId;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public String getRequesterRole() {
        return requesterRole;
    }

    public void setRequesterRole(String requesterRole) {
        this.requesterRole = requesterRole;
    }

    public String getRequesterIdentityType() {
        return requesterIdentityType;
    }

    public void setRequesterIdentityType(String requesterIdentityType) {
        this.requesterIdentityType = requesterIdentityType;
    }

    public Integer getRequesterIdentityId() {
        return requesterIdentityId;
    }

    public void setRequesterIdentityId(Integer requesterIdentityId) {
        this.requesterIdentityId = requesterIdentityId;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getHandledByUserId() {
        return handledByUserId;
    }

    public void setHandledByUserId(Integer handledByUserId) {
        this.handledByUserId = handledByUserId;
    }

    public String getHandledByUsername() {
        return handledByUsername;
    }

    public void setHandledByUsername(String handledByUsername) {
        this.handledByUsername = handledByUsername;
    }

    public LocalDateTime getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }

    public String getHandleComment() {
        return handleComment;
    }

    public void setHandleComment(String handleComment) {
        this.handleComment = handleComment;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }
}
