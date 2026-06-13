package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.HandleRequestTicketRequest;
import com.example.demo.entity.RequestTicket;
import com.example.demo.entity.UserAccount;
import com.example.demo.service.AuthService;
import com.example.demo.service.RequestTicketService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 管理员请求中心控制器：负责铃铛未读数、请求列表、详情和审批处理。
@RestController
@Validated
// 管理员相关请求接口统一挂在 /admin 下，并声明 JSON 响应使用 UTF-8。
@RequestMapping(value = "/admin", produces = "application/json;charset=UTF-8")
public class AdminRequestController {
    private final RequestTicketService requestTicketService;
    private final AuthService authService;

    // 构造方法注入请求服务和认证服务。
    public AdminRequestController(RequestTicketService requestTicketService, AuthService authService) {
        this.requestTicketService = requestTicketService;
        this.authService = authService;
    }

    // GET /admin/notifications/unread-count：统计管理员铃铛里的未读请求数。
    @GetMapping("/notifications/unread-count")
    public Result<Integer> countUnread(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success(requestTicketService.countUnread());
    }

    // GET /admin/requests：管理员查看全部请求列表。
    @GetMapping("/requests")
    public Result<List<RequestTicket>> findAllRequests(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success(requestTicketService.findAdminRequests());
    }

    // GET /admin/requests/{id}：管理员查看某一条请求详情。
    @GetMapping("/requests/{id}")
    public Result<RequestTicket> findRequestDetail(@PathVariable @Min(value = 1, message = "请求id必须大于0") Long id,
                                                   HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success(requestTicketService.getRequestById(id));
    }

    // PUT /admin/requests/{id}/read：管理员将某条请求标记为已读。
    @PutMapping("/requests/{id}/read")
    public Result<String> markRead(@PathVariable @Min(value = 1, message = "请求id必须大于0") Long id,
                                   HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        requestTicketService.markRead(id);
        return Result.success("已标记已读", "ok");
    }

    // PUT /admin/requests/{id}/handle：管理员审批处理请求。
    @PutMapping("/requests/{id}/handle")
    public Result<RequestTicket> handleRequest(@PathVariable @Min(value = 1, message = "请求id必须大于0") Long id,
                                               @Valid @RequestBody HandleRequestTicketRequest request,
                                               HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success("请求处理成功", requestTicketService.handleRequest(id, currentUser, request));
    }
}
