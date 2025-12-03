package com.group7.marketplacesystem.communication.service;

import com.group7.marketplacesystem.communication.dto.request.NotificationRequest;
import com.group7.marketplacesystem.communication.dto.response.NotificationResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface NotificationService {
    NotificationResponse createNotification(NotificationRequest request, Integer adminId);

    List<NotificationResponse> getAllNotifications();

    NotificationResponse getNotificationById(Integer id);

    void deleteNotification(Integer id);

    NotificationResponse updateNotification(Integer id, NotificationRequest request);

    void sendNotification(Integer notificationId, String target);

    List<String> getRecipients(Integer notificationId);
}
