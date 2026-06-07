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

@RestController
@Validated
@RequestMapping(value = "/requests", produces = "application/json;charset=UTF-8")
public class RequestTicketController {
    private final RequestTicketService requestTicketService;
    private final AuthService authService;

    public RequestTicketController(RequestTicketService requestTicketService, AuthService authService) {
        this.requestTicketService = requestTicketService;
        this.authService = authService;
    }

    @PostMapping
    public Result<RequestTicket> createRequest(@Valid @RequestBody CreateRequestTicketRequest request,
                                               HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        return Result.success("请求提交成功", requestTicketService.createRequest(currentUser, request));
    }

    @GetMapping("/my")
    public Result<List<RequestTicket>> findMyRequests(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        return Result.success(requestTicketService.findMyRequests(currentUser));
    }

    @GetMapping("/my/{id}")
    public Result<RequestTicket> findMyRequestDetail(@PathVariable @Min(value = 1, message = "请求id必须大于0") Long id,
                                                     HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        RequestTicket ticket = requestTicketService.getRequestById(id);
        if (!ticket.getRequesterUserId().equals(currentUser.getId())) {
            throw new com.example.demo.exception.BusinessException(403, "只能查看自己发起的请求");
        }
        return Result.success(ticket);
    }
}
