package com.group7.marketplacesystem.communication.mapper;

import com.group7.marketplacesystem.communication.dto.response.NotificationUserResponse;
import com.group7.marketplacesystem.communication.entity.Notificationuser;
import org.springframework.stereotype.Component;

@Component
public class NotificationUserMapper {

    public NotificationUserResponse toResponse(Notificationuser notificationuser){
        if(notificationuser == null){
            return null;
        }
        return NotificationUserResponse.builder()
                .id(notificationuser.getId().getNotificationId())
                .title(notificationuser.getNotification().getTitle())
                .message(notificationuser.getNotification().getMessage())
                .createdAt(notificationuser.getNotification().getCreatedAt())
                .isRead(notificationuser.getIsRead())
                .build();
    }
}
