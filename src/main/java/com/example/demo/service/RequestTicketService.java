package com.example.demo.service;

import com.example.demo.dto.CreateRequestTicketRequest;
import com.example.demo.dto.HandleRequestTicketRequest;
import com.example.demo.entity.AdminNotification;
import com.example.demo.entity.RequestTicket;
import com.example.demo.entity.UserAccount;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.AdminNotificationMapper;
import com.example.demo.mapper.RequestTicketMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestTicketService {
    private final RequestTicketMapper requestTicketMapper;
    private final AdminNotificationMapper adminNotificationMapper;
    private final StudentScoreService studentScoreService;

    public RequestTicketService(RequestTicketMapper requestTicketMapper,
                                AdminNotificationMapper adminNotificationMapper,
                                StudentScoreService studentScoreService) {
        this.requestTicketMapper = requestTicketMapper;
        this.adminNotificationMapper = adminNotificationMapper;
        this.studentScoreService = studentScoreService;
    }

    public RequestTicket createRequest(UserAccount currentUser, CreateRequestTicketRequest request) {
        RequestTicket ticket = new RequestTicket();
        ticket.setRequesterUserId(currentUser.getId());
        ticket.setRequesterUsername(currentUser.getUsername());
        ticket.setRequesterRole(currentUser.getRole());
        ticket.setRequesterIdentityType(currentUser.getIdentityType());
        ticket.setRequesterIdentityId(currentUser.getIdentityId());
        ticket.setRequestType(normalizeRequestType(request.getRequestType()));
        ticket.setTitle(request.getTitle().trim());
        ticket.setContent(request.getContent().trim());
        ticket.setRelatedEntityType(normalizeRelatedEntityType(request.getRelatedEntityType()));
        ticket.setRelatedEntityId(request.getRelatedEntityId());
        ticket.setExtraPayloadJson(StringUtils.hasText(request.getExtraPayloadJson()) ? request.getExtraPayloadJson().trim() : null);
        ticket.setStatus("PENDING");

        validateRequest(currentUser, ticket);

        int rows = requestTicketMapper.insertRequestTicket(ticket);
        if (rows <= 0 || ticket.getId() == null) {
            throw new BusinessException(500, "提交请求失败");
        }

        AdminNotification notification = new AdminNotification();
        notification.setRequestId(ticket.getId());
        notification.setReceiverRole("ADMIN");
        notification.setRead(false);
        adminNotificationMapper.insertNotification(notification);
        return getRequestById(ticket.getId());
    }

    public List<RequestTicket> findMyRequests(UserAccount currentUser) {
        return requestTicketMapper.findMyRequests(currentUser.getId());
    }

    public List<RequestTicket> findAdminRequests() {
        return requestTicketMapper.findAdminRequests();
    }

    public RequestTicket getRequestById(Long id) {
        RequestTicket ticket = requestTicketMapper.findById(id);
        if (ticket == null) {
            throw new BusinessException(404, "请求不存在");
        }
        return ticket;
    }

    public int countUnread() {
        return adminNotificationMapper.countUnread();
    }

    public void markRead(Long requestId) {
        getRequestById(requestId);
        adminNotificationMapper.markReadByRequestId(requestId);
    }

    public RequestTicket handleRequest(Long requestId, UserAccount currentUser, HandleRequestTicketRequest request) {
        RequestTicket existing = getRequestById(requestId);
        existing.setStatus(normalizeHandleStatus(request.getStatus()));
        existing.setHandledByUserId(currentUser.getId());
        existing.setHandledByUsername(currentUser.getUsername());
        existing.setHandledAt(LocalDateTime.now());
        existing.setHandleComment(StringUtils.hasText(request.getHandleComment()) ? request.getHandleComment().trim() : null);

        int rows = requestTicketMapper.updateHandledFields(existing);
        if (rows <= 0) {
            throw new BusinessException(500, "处理请求失败");
        }
        adminNotificationMapper.markReadByRequestId(requestId);
        return getRequestById(requestId);
    }

    private void validateRequest(UserAccount currentUser, RequestTicket ticket) {
        if ("STUDENT_SCORE".equals(ticket.getRelatedEntityType())) {
            if (ticket.getRelatedEntityId() == null || ticket.getRelatedEntityId() <= 0) {
                throw new BusinessException(400, "成绩请求必须提供有效的成绩记录ID");
            }
            Integer studentId = studentScoreService.findById(ticket.getRelatedEntityId()).getStudentId();
            authScoreRequestScope(currentUser, studentId, ticket.getRequestType());
        }
    }

    private void authScoreRequestScope(UserAccount currentUser, Integer studentId, String requestType) {
        if ("GRADE_UPDATE".equals(requestType)) {
            if (!currentUser.isAdmin() && !"TEACHER".equalsIgnoreCase(currentUser.getIdentityType())) {
                throw new BusinessException(403, "只有管理员或老师可以提交成绩更正请求");
            }
            return;
        }
        if ("GRADE_REVIEW".equals(requestType)) {
            if (!"STUDENT".equalsIgnoreCase(currentUser.getIdentityType()) || currentUser.getIdentityId() == null || !currentUser.getIdentityId().equals(studentId)) {
                throw new BusinessException(403, "学生只能为自己的成绩提交复核请求");
            }
            return;
        }
        if (!currentUser.isAdmin() && !"TEACHER".equalsIgnoreCase(currentUser.getIdentityType()) && !"STUDENT".equalsIgnoreCase(currentUser.getIdentityType())) {
            throw new BusinessException(403, "当前账号不能提交该类型请求");
        }
    }

    private String normalizeRequestType(String requestType) {
        String normalized = requestType == null ? "" : requestType.trim().toUpperCase();
        if (!"GRADE_REVIEW".equals(normalized) && !"GRADE_UPDATE".equals(normalized) && !"GENERAL_APPLICATION".equals(normalized)) {
            throw new BusinessException(400, "请求类型不支持");
        }
        return normalized;
    }

    private String normalizeRelatedEntityType(String relatedEntityType) {
        if (!StringUtils.hasText(relatedEntityType)) {
            return null;
        }
        String normalized = relatedEntityType.trim().toUpperCase();
        if (!"STUDENT_SCORE".equals(normalized) && !"STUDENT".equals(normalized) && !"TEACHER".equals(normalized)) {
            throw new BusinessException(400, "关联实体类型不支持");
        }
        return normalized;
    }

    private String normalizeHandleStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!"PENDING".equals(normalized) && !"PROCESSING".equals(normalized) && !"APPROVED".equals(normalized) && !"REJECTED".equals(normalized)) {
            throw new BusinessException(400, "处理状态不支持");
        }
        return normalized;
    }
}
