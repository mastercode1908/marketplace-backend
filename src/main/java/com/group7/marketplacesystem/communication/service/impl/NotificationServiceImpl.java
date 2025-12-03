package com.group7.marketplacesystem.communication.service.impl;

import ch.qos.logback.classic.spi.IThrowableProxy;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.communication.dto.request.NotificationRequest;
import com.group7.marketplacesystem.communication.dto.response.NotificationResponse;
import com.group7.marketplacesystem.communication.entity.Notification;
import com.group7.marketplacesystem.communication.entity.Notificationuser;
import com.group7.marketplacesystem.communication.entity.NotificationuserId;
import com.group7.marketplacesystem.communication.mapper.NotificationMapper;
import com.group7.marketplacesystem.communication.repository.NotificationRepository;
import com.group7.marketplacesystem.communication.repository.NotificationUserRepository;
import com.group7.marketplacesystem.communication.service.NotificationService;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.infrastructure.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationUserRepository notificationUserRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final MailService mailService;

    @Override
    public NotificationResponse createNotification(NotificationRequest request, Integer adminId) {
        //  Validate dữ liệu đầu vào
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_TITLE);
        }

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_MESSAGE);
        }

        if (request.getType() == null || request.getType().trim().isEmpty()) {
            request.setType("System");
        }
        // Map request -> entity
        Notification notification = notificationMapper.toEntity(request, adminId);
        notification.setTitle(validateAndNormalize(request.getTitle(), ErrorCode.INVALID_TITLE));
        notification.setMessage(validateAndNormalize(request.getMessage(), ErrorCode.INVALID_MESSAGE));
        notification.setCreatedAt(Instant.now());
        notification.setUpdatedAt(Instant.now());
        notification.setDeletedAt(null);

        // Lưu thông báo vào DB
        Notification savedNotification = notificationRepository.save(notification);

        // Trả kết quả ra ngoài
        return notificationMapper.toResponse(savedNotification);

    }

    @Override
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .filter(n -> n.getDeletedAt() == null) // chỉ lấy chưa xóa
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public NotificationResponse getNotificationById(Integer id) {
        if (id == null || id <= 0) {
            throw new ApiException(ErrorCode.INVALID_KEY);
        }

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOTIFICATION_NOT_FOUND));

        return notificationMapper.toResponse(notification);

    }

    @Override
    public void deleteNotification(Integer id) {
        if (id == null || id <= 0) {
            throw new ApiException(ErrorCode.INVALID_KEY);
        }

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (notification.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        // Soft delete
        notification.setDeletedAt(Instant.now());
        notificationRepository.save(notification);
    }

    @Override
    public NotificationResponse updateNotification(Integer id, NotificationRequest request) {
        if (id == null || id <= 0) {
            throw new ApiException(ErrorCode.INVALID_KEY);
        }
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (notification.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        if (request.getTitle() != null || !request.getTitle().trim().isEmpty()) {

            notification.setTitle(validateAndNormalize(request.getTitle(), ErrorCode.INVALID_TITLE));
        }
        if (request.getMessage() != null || !request.getMessage().trim().isEmpty()) {
            notification.setMessage(validateAndNormalize(request.getMessage(), ErrorCode.INVALID_MESSAGE));
        }

        notification.setType(request.getType());


        notification.setUpdatedAt(Instant.now());
        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponse(saved);
    }

    @Override
    public void sendNotification(Integer notificationId, String target) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOTIFICATION_NOT_FOUND));
        List<User> targetUser = new ArrayList<>();

        if (notification.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        try {
            int userId = Integer.parseInt(target); // thử convert thành số
            targetUser.add(userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND)));
        } catch (NumberFormatException e) {
            // không phải số → check các từ khóa đặc biệt
            if (target.equalsIgnoreCase("ALL")) {
                targetUser = userRepository.findAllActiveUsers();
            } else if (target.equalsIgnoreCase("BUYER")) {
                targetUser = userRepository.findAllBuyer();
            } else if (target.equalsIgnoreCase("SELLER")) {
                targetUser = userRepository.findBySeller();
            } else {
                throw new ApiException(ErrorCode.INVALID_KEY);
            }
        }
        sendNotificationToUsers(notification, targetUser);

        // Update sent timestamp only on first send
        if (notification.getSentAt() == null) {
            notification.setSentAt(Instant.now());
            notificationRepository.save(notification);
        }
    }

    public void sendNotificationToUsers(Notification notification, List<User> targetUsers) {
        boolean sendEmail = targetUsers.size() == 1; // Only send email for individual user

        for (User user : targetUsers) {
            
            // Check if user already received this notification (prevent spam/duplicate)
            NotificationuserId nuId = new NotificationuserId();
            nuId.setUserId(user.getId());
            nuId.setNotificationId(notification.getId());
            
            if (notificationUserRepository.existsById(nuId)) {
                // User already received this notification, skip
                continue;
            }

            Notificationuser nu = new Notificationuser();
            nu.setId(nuId);
            nu.setUser(user);
            nu.setNotification(notification);
            nu.setIsRead(false);
            notificationUserRepository.save(nu);

            // Gửi WebSocket real-time
            NotificationResponse dto = notificationMapper.toResponse(notification);
            messagingTemplate.convertAndSend(
                    "/topic/notification/" + user.getId(),
                    dto
            );

            // Only send email if targeting single user
            if (sendEmail) {
                mailService.sendNotificationEmailToUsers(
                        user.getEmail(),
                        notification
                );
            }
        }
    }

    @Override
    public List<String> getRecipients(Integer notificationId) {
        List<Notificationuser> notificationUsers = notificationUserRepository.findByNotificationId(notificationId);
        return notificationUsers.stream()
                .map(nu -> nu.getUser().getUsername() + " (" + nu.getUser().getEmail() + ") - " + nu.getUser().getRole())
                .toList();
    }

    private String validateAndNormalize(String value, ErrorCode error) {
        if (value == null) return null;

        // Không cho phép space đầu
        if (value.startsWith(" ")) {
            throw new ApiException(error);
        }

        String cleaned = value.trim();

        if (cleaned.isEmpty()) {
            throw new ApiException(error);
        }

        // Không cho phép 2 dấu cách liên tiếp
        if (cleaned.matches(".*\\s{2,}.*")) {
            throw new ApiException(error);
        }

        // Cho phép: chữ, số, space, và các dấu câu phổ biến: ! / . , ? : ; - ( )
        if (!cleaned.matches("^[\\p{L}\\p{N}\\s!/.,:;?()\\-]+$")) {
            throw new ApiException(error);
        }

        return cleaned;
    }


}