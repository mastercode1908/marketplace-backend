package com.group7.marketplacesystem.communication.mapper;

import com.group7.marketplacesystem.communication.dto.request.NotificationRequest;
import com.group7.marketplacesystem.communication.dto.response.NotificationResponse;
import com.group7.marketplacesystem.communication.entity.Notification;
import com.group7.marketplacesystem.communication.repository.NotificationUserRepository;
import com.group7.marketplacesystem.identity.entity.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final NotificationUserRepository notificationUserRepository;

    public Notification toEntity(NotificationRequest dto, Integer adminId){
        if(dto == null){
            return null;
        }
        Notification notification = new Notification();
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setType(dto.getType() != null ? dto.getType() : "System");

        Admin admin = new Admin();
        admin.setId(adminId);
        notification.setAdmin(admin);
        return notification;
    }

    public NotificationResponse toResponse(Notification notification){
        if(notification == null) return null;
        long recipientCount = notificationUserRepository.countById_NotificationId(notification.getId());
        
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .sentAt(notification.getSentAt())
                .recipientCount(recipientCount)
                .build();
    }

}
