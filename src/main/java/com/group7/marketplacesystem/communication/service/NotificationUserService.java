package com.group7.marketplacesystem.communication.service;

import com.group7.marketplacesystem.communication.dto.response.NotificationUserResponse;

import java.util.List;

public interface NotificationUserService {
    NotificationUserResponse markAsRead(Integer notificationId, Integer userId);
    void markAllRead(Integer userId);
    List<NotificationUserResponse>getAllNotificationByUser(Integer userId);
    void hideReadNotification(Integer userId);
    Long countUnreadByUserId(Integer userId);
}
