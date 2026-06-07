package com.example.demo.mapper;

import com.example.demo.entity.AdminNotification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminNotificationMapper {
    int insertNotification(AdminNotification notification);

    int countUnread();

    int markReadByRequestId(Long requestId);
}
