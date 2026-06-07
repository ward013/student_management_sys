package com.example.demo.mapper;

import com.example.demo.entity.RequestTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RequestTicketMapper {
    int insertRequestTicket(RequestTicket requestTicket);

    RequestTicket findById(Long id);

    List<RequestTicket> findMyRequests(Integer requesterUserId);

    List<RequestTicket> findAdminRequests();

    int updateHandledFields(RequestTicket requestTicket);
}
