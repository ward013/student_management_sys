package com.example.demo.mapper;

import com.example.demo.entity.RequestTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// RequestTicketMapper 负责 request_ticket 表及其与通知表联查的请求中心数据。
@Mapper
public interface RequestTicketMapper {
    // 新增一条请求记录。
    int insertRequestTicket(RequestTicket requestTicket);

    // 根据请求 id 查询一条请求详情。
    RequestTicket findById(Long id);

    // 查询当前用户自己提交的请求列表。
    List<RequestTicket> findMyRequests(Integer requesterUserId);

    // 管理员查询全部请求列表。
    List<RequestTicket> findAdminRequests();

    // 更新管理员审批后的状态和处理信息。
    int updateHandledFields(RequestTicket requestTicket);
}
