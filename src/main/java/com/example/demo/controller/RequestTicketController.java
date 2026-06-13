package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.CreateRequestTicketRequest;
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

// 请求控制器：普通用户在这里提交请求，并查看自己发起的请求记录。
@RestController
@Validated
// 给本控制器下的所有接口统一加上 /requests 前缀，并声明 JSON 响应使用 UTF-8。
@RequestMapping(value = "/requests", produces = "application/json;charset=UTF-8")
public class RequestTicketController {
    private final RequestTicketService requestTicketService;
    private final AuthService authService;

    // 构造方法注入请求服务和认证服务。
    public RequestTicketController(RequestTicketService requestTicketService, AuthService authService) {
        this.requestTicketService = requestTicketService;
        this.authService = authService;
    }

    // POST /requests：提交一条新的业务请求，例如成绩复核或成绩更正。
    @PostMapping
    public Result<RequestTicket> createRequest(@Valid @RequestBody CreateRequestTicketRequest request,
                                               HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        return Result.success("请求提交成功", requestTicketService.createRequest(currentUser, request));
    }

    // GET /requests/my：查询当前登录用户自己发起的全部请求。
    @GetMapping("/my")
    public Result<List<RequestTicket>> findMyRequests(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        return Result.success(requestTicketService.findMyRequests(currentUser));
    }

    // GET /requests/my/{id}：查看自己发起的一条请求详情。
    @GetMapping("/my/{id}")
    public Result<RequestTicket> findMyRequestDetail(@PathVariable @Min(value = 1, message = "请求id必须大于0") Long id,
                                                     HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        RequestTicket ticket = requestTicketService.getRequestById(id);
        // 普通用户只能查看自己发起的请求，不能看别人的记录。
        if (!ticket.getRequesterUserId().equals(currentUser.getId())) {
            throw new com.example.demo.exception.BusinessException(403, "只能查看自己发起的请求");
        }
        return Result.success(ticket);
    }
}
