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

@RestController
@Validated
@RequestMapping(value = "/admin", produces = "application/json;charset=UTF-8")
public class AdminRequestController {
    private final RequestTicketService requestTicketService;
    private final AuthService authService;

    public AdminRequestController(RequestTicketService requestTicketService, AuthService authService) {
        this.requestTicketService = requestTicketService;
        this.authService = authService;
    }

    @GetMapping("/notifications/unread-count")
    public Result<Integer> countUnread(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success(requestTicketService.countUnread());
    }

    @GetMapping("/requests")
    public Result<List<RequestTicket>> findAllRequests(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success(requestTicketService.findAdminRequests());
    }

    @GetMapping("/requests/{id}")
    public Result<RequestTicket> findRequestDetail(@PathVariable @Min(value = 1, message = "请求id必须大于0") Long id,
                                                   HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success(requestTicketService.getRequestById(id));
    }

    @PutMapping("/requests/{id}/read")
    public Result<String> markRead(@PathVariable @Min(value = 1, message = "请求id必须大于0") Long id,
                                   HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        requestTicketService.markRead(id);
        return Result.success("已标记已读", "ok");
    }

    @PutMapping("/requests/{id}/handle")
    public Result<RequestTicket> handleRequest(@PathVariable @Min(value = 1, message = "请求id必须大于0") Long id,
                                               @Valid @RequestBody HandleRequestTicketRequest request,
                                               HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success("请求处理成功", requestTicketService.handleRequest(id, currentUser, request));
    }
}
