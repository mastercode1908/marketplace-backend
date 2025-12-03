package com.group7.marketplacesystem.communication.service.impl;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.communication.dto.response.NotificationUserResponse;
import com.group7.marketplacesystem.communication.entity.Notificationuser;
import com.group7.marketplacesystem.communication.entity.NotificationuserId;
import com.group7.marketplacesystem.communication.mapper.NotificationUserMapper;
import com.group7.marketplacesystem.communication.repository.NotificationUserRepository;
import com.group7.marketplacesystem.communication.service.NotificationUserService;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class NotificationUserServiceImpl implements NotificationUserService {

    private final NotificationUserRepository notificationUserRepository;
    private final NotificationUserMapper notificationUserMapper;
    private final UserRepository userRepository;

    @Override
    public NotificationUserResponse markAsRead(Integer notificationId, Integer userId) {
        NotificationuserId id = new NotificationuserId();
        id.setNotificationId(notificationId);
        id.setUserId(userId);
        Notificationuser notificationuser = notificationUserRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notificationuser.setReadAt(Instant.now());
        notificationuser.setIsRead(true);

        notificationUserRepository.save(notificationuser);
        
        // Use mapper instead of native query to avoid projection issues
        return notificationUserMapper.toResponse(notificationuser);
    }

    @Override
    public void markAllRead(Integer userId) {
        List<Notificationuser> notifications = notificationUserRepository.findAllById_UserId(userId);

        if (notifications.isEmpty()) {
            throw new ApiException(ErrorCode.USER_NOT_EXISTED);
        }

        notifications.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(Instant.now());
        });

        notificationUserRepository.saveAll(notifications);
    }

    @Override
    public List<NotificationUserResponse> getAllNotificationByUser(Integer userId) {
        List<Notificationuser> notifications = notificationUserRepository.findAllById_UserId(userId)
                .stream()
                .filter(n -> !Boolean.TRUE.equals(n.getHidden())
                        && n.getNotification().getDeletedAt() == null) // chỉ lấy chưa ẩn và chưa bị xóa
                .toList();

        return notifications.stream()
                .map(notificationUserMapper::toResponse)
                .toList();
    }

    @Override
    public void hideReadNotification(Integer userId) {
        List<Notificationuser> readNotification = notificationUserRepository.findAllById_UserId(userId)
                .stream()
                .filter(Notificationuser::getIsRead)
                .toList();
        readNotification.forEach(n -> n.setHidden(true));

        notificationUserRepository.saveAll(readNotification);
    }

    @Override
    public Long countUnreadByUserId(Integer userId) {
        return notificationUserRepository.findAllById_UserId(userId)
                .stream()
                .filter(n -> !Boolean.TRUE.equals(n.getHidden()) && !Boolean.TRUE.equals(n.getIsRead())
                        && n.getNotification().getDeletedAt() == null)
                .count();
    }
}
