package com.example.demo.mapper;

import com.example.demo.entity.AdminNotification;
import org.apache.ibatis.annotations.Mapper;

// AdminNotificationMapper 负责 admin_notification 表的插入、统计和已读更新。
@Mapper
public interface AdminNotificationMapper {
    // 插入一条管理员通知。
    int insertNotification(AdminNotification notification);

    // 统计管理员当前未读通知数。
    int countUnread();

    // 根据请求 id 把对应管理员通知标记为已读。
    int markReadByRequestId(Long requestId);
}
