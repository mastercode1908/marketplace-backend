package com.group7.marketplacesystem.communication.controller;

import com.group7.marketplacesystem.common.constants.MessageConstants;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.response.ApiResponse;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.communication.dto.response.NotificationUserResponse;
import com.group7.marketplacesystem.communication.entity.Notification;
import com.group7.marketplacesystem.communication.entity.Notificationuser;
import com.group7.marketplacesystem.communication.entity.NotificationuserId;
import com.group7.marketplacesystem.communication.repository.NotificationRepository;
import com.group7.marketplacesystem.communication.repository.NotificationUserRepository;
import com.group7.marketplacesystem.communication.service.NotificationUserService;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.group7.marketplacesystem.common.constants.MessageConstants.*;

@RestController
@RequestMapping("/api/users/notifications")
@AllArgsConstructor
public class NotificationUserController {
    private final NotificationUserService notificationUserService;


    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationUserResponse>>markAsRead(@PathVariable Integer notificationId){
        NotificationUserResponse notificationUserResponse = notificationUserService.markAsRead(notificationId, getCurrentUserId());
        return  ResponseEntity.ok(ApiResponse.success(NOTIFICATION_MARK_AS_READ_SUCCESS,notificationUserResponse));
    }

    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<String>>markAsReadAll(){
        notificationUserService.markAllRead(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(NOTIFICATION_MARK_ALL_AS_READ_SUCCESS, "Tất cả thông báo đã được đọc!"));
    }

    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    @PutMapping("/hide-read")
    public ResponseEntity<ApiResponse<String>>hideReadNotification(){
        notificationUserService.hideReadNotification(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(NOTIFICATION_MARK_ALL_AS_READ_HIDE, "Đã ẩn tất cả thông báo đã đọc."));
    }

    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationUserResponse>>>getAllNotification(){
       try {
           List<NotificationUserResponse> response = notificationUserService.getAllNotificationByUser(getCurrentUserId());
           return ResponseEntity.ok(ApiResponse.success(response));
       }catch (Exception e){
           return ResponseEntity.internalServerError().body(ApiResponse.fail(NOTIFICATION_NOT_FOUND + e.getMessage()));
       }
    }

    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>>getUnreadCount(){
        Long count = notificationUserService.countUnreadByUserId(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }

}
