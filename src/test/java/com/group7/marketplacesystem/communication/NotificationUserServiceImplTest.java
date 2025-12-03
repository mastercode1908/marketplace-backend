package com.group7.marketplacesystem.communication;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.communication.dto.response.NotificationUserResponse;
import com.group7.marketplacesystem.communication.entity.Notification;
import com.group7.marketplacesystem.communication.entity.Notificationuser;
import com.group7.marketplacesystem.communication.entity.NotificationuserId;
import com.group7.marketplacesystem.communication.mapper.NotificationUserMapper;
import com.group7.marketplacesystem.communication.repository.NotificationUserRepository;
import com.group7.marketplacesystem.communication.service.impl.NotificationUserServiceImpl;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification User Service Tests")
class NotificationUserServiceImplTest {

    @Mock
    private NotificationUserRepository notificationUserRepository;

    @Mock
    private NotificationUserMapper notificationUserMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationUserServiceImpl notificationUserService;

    private Notificationuser notificationUser;
    private NotificationuserId notificationuserId;
    private NotificationUserResponse notificationUserResponse;
    private Notification notification;

    @BeforeEach
    void setUp() {
        // Setup notification
        notification = new Notification();
        notification.setId(1);
        notification.setTitle("Đơn hàng mới");
        notification.setMessage("Bạn có đơn hàng mới #12345");
        notification.setCreatedAt(Instant.now());

        // Setup notification user ID
        notificationuserId = new NotificationuserId();
        notificationuserId.setNotificationId(1);
        notificationuserId.setUserId(1);

        // Setup notification user
        notificationUser = new Notificationuser();
        notificationUser.setId(notificationuserId);
        notificationUser.setNotification(notification);
        notificationUser.setIsRead(false);
        notificationUser.setHidden(false);

        // Setup response
        notificationUserResponse = new NotificationUserResponse();
        notificationUserResponse.setTitle("Đơn hàng mới");
        notificationUserResponse.setMessage("Bạn có đơn hàng mới #12345");
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void testMarkAsRead_Success() {
        // Given
        Integer notificationId = 1;
        Integer userId = 1;
        
        when(notificationUserRepository.findById(any(NotificationuserId.class)))
            .thenReturn(Optional.of(notificationUser));
        when(notificationUserRepository.save(any(Notificationuser.class)))
            .thenReturn(notificationUser);
        when(notificationUserRepository.getNotification(anyInt(), anyInt()))
            .thenReturn(notificationUserResponse);

        // When
        NotificationUserResponse result = notificationUserService.markAsRead(notificationId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(notificationUser.getIsRead()).isTrue();
        assertThat(notificationUser.getReadAt()).isNotNull();
        
        verify(notificationUserRepository, times(1)).findById(any(NotificationuserId.class));
        verify(notificationUserRepository, times(1)).save(any(Notificationuser.class));
    }

    @Test
    @DisplayName("Should throw exception when marking non-existent notification as read")
    void testMarkAsRead_NotificationNotFound() {
        // Given
        Integer notificationId = 999;
        Integer userId = 1;
        
        when(notificationUserRepository.findById(any(NotificationuserId.class)))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationUserService.markAsRead(notificationId, userId))
            .isInstanceOf(ApiException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND);
        
        verify(notificationUserRepository, times(1)).findById(any(NotificationuserId.class));
        verify(notificationUserRepository, never()).save(any(Notificationuser.class));
    }

    @Test
    @DisplayName("Should mark all notifications as read successfully")
    void testMarkAllRead_Success() {
        // Given
        Integer userId = 1;
        Notificationuser notif1 = createNotificationUser(1, userId, false);
        Notificationuser notif2 = createNotificationUser(2, userId, false);
        Notificationuser notif3 = createNotificationUser(3, userId, false);
        
        List<Notificationuser> notifications = Arrays.asList(notif1, notif2, notif3);
        
        when(notificationUserRepository.findAllById_UserId(anyInt()))
            .thenReturn(notifications);
        when(notificationUserRepository.saveAll(anyList()))
            .thenReturn(notifications);

        // When
        notificationUserService.markAllRead(userId);

        // Then
        notifications.forEach(n -> {
            assertThat(n.getIsRead()).isTrue();
            assertThat(n.getReadAt()).isNotNull();
        });
        
        verify(notificationUserRepository, times(1)).findAllById_UserId(userId);
        verify(notificationUserRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when marking all read for non-existent user")
    void testMarkAllRead_UserNotFound() {
        // Given
        Integer userId = 999;
        
        when(notificationUserRepository.findAllById_UserId(anyInt()))
            .thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> notificationUserService.markAllRead(userId))
            .isInstanceOf(ApiException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);
        
        verify(notificationUserRepository, times(1)).findAllById_UserId(userId);
        verify(notificationUserRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should get all notifications for user successfully")
    void testGetAllNotificationByUser_Success() {
        // Given
        Integer userId = 1;
        Notificationuser notif1 = createNotificationUser(1, userId, false);
        Notificationuser notif2 = createNotificationUser(2, userId, true);
        
        List<Notificationuser> notifications = Arrays.asList(notif1, notif2);
        
        when(notificationUserRepository.findAllById_UserId(anyInt()))
            .thenReturn(notifications);
        when(notificationUserMapper.toResponse(any(Notificationuser.class)))
            .thenReturn(notificationUserResponse);

        // When
        List<NotificationUserResponse> result = notificationUserService.getAllNotificationByUser(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        verify(notificationUserRepository, times(1)).findAllById_UserId(userId);
        verify(notificationUserMapper, times(2)).toResponse(any(Notificationuser.class));
    }

    @Test
    @DisplayName("Should filter out hidden and deleted notifications")
    void testGetAllNotificationByUser_FilterHiddenAndDeleted() {
        // Given
        Integer userId = 1;
        
        Notificationuser visibleNotif = createNotificationUser(1, userId, false);
        visibleNotif.setHidden(false);
        
        Notificationuser hiddenNotif = createNotificationUser(2, userId, false);
        hiddenNotif.setHidden(true);
        
        Notificationuser deletedNotif = createNotificationUser(3, userId, false);
        deletedNotif.getNotification().setDeletedAt(Instant.now());
        
        List<Notificationuser> notifications = Arrays.asList(visibleNotif, hiddenNotif, deletedNotif);
        
        when(notificationUserRepository.findAllById_UserId(anyInt()))
            .thenReturn(notifications);
        when(notificationUserMapper.toResponse(any(Notificationuser.class)))
            .thenReturn(notificationUserResponse);

        // When
        List<NotificationUserResponse> result = notificationUserService.getAllNotificationByUser(userId);

        // Then
        assertThat(result).hasSize(1); // Only visible notification
        verify(notificationUserMapper, times(1)).toResponse(any(Notificationuser.class));
    }

    @Test
    @DisplayName("Should hide read notifications successfully")
    void testHideReadNotification_Success() {
        // Given
        Integer userId = 1;
        Notificationuser readNotif1 = createNotificationUser(1, userId, true);
        Notificationuser readNotif2 = createNotificationUser(2, userId, true);
        Notificationuser unreadNotif = createNotificationUser(3, userId, false);
        
        List<Notificationuser> allNotifications = Arrays.asList(readNotif1, readNotif2, unreadNotif);
        
        when(notificationUserRepository.findAllById_UserId(anyInt()))
            .thenReturn(allNotifications);
        when(notificationUserRepository.saveAll(anyList()))
            .thenReturn(Arrays.asList(readNotif1, readNotif2));

        // When
        notificationUserService.hideReadNotification(userId);

        // Then
        assertThat(readNotif1.getHidden()).isTrue();
        assertThat(readNotif2.getHidden()).isTrue();
        assertThat(unreadNotif.getHidden()).isFalse();
        
        verify(notificationUserRepository, times(1)).findAllById_UserId(userId);
        verify(notificationUserRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should count unread notifications correctly")
    void testCountUnreadByUserId_Success() {
        // Given
        Integer userId = 1;
        
        Notificationuser unread1 = createNotificationUser(1, userId, false);
        unread1.setHidden(false);
        
        Notificationuser unread2 = createNotificationUser(2, userId, false);
        unread2.setHidden(false);
        
        Notificationuser readNotif = createNotificationUser(3, userId, true);
        readNotif.setHidden(false);
        
        Notificationuser hiddenNotif = createNotificationUser(4, userId, false);
        hiddenNotif.setHidden(true);
        
        List<Notificationuser> notifications = Arrays.asList(unread1, unread2, readNotif, hiddenNotif);
        
        when(notificationUserRepository.findAllById_UserId(anyInt()))
            .thenReturn(notifications);

        // When
        Long count = notificationUserService.countUnreadByUserId(userId);

        // Then
        assertThat(count).isEqualTo(2); // Only 2 unread, visible, non-deleted notifications
        verify(notificationUserRepository, times(1)).findAllById_UserId(userId);
    }

    @Test
    @DisplayName("Should return zero when no unread notifications")
    void testCountUnreadByUserId_NoUnread() {
        // Given
        Integer userId = 1;
        
        Notificationuser readNotif1 = createNotificationUser(1, userId, true);
        Notificationuser readNotif2 = createNotificationUser(2, userId, true);
        
        List<Notificationuser> notifications = Arrays.asList(readNotif1, readNotif2);
        
        when(notificationUserRepository.findAllById_UserId(anyInt()))
            .thenReturn(notifications);

        // When
        Long count = notificationUserService.countUnreadByUserId(userId);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Should exclude deleted notifications from count")
    void testCountUnreadByUserId_ExcludeDeleted() {
        // Given
        Integer userId = 1;
        
        Notificationuser unreadNotif = createNotificationUser(1, userId, false);
        unreadNotif.setHidden(false);
        
        Notificationuser deletedUnreadNotif = createNotificationUser(2, userId, false);
        deletedUnreadNotif.setHidden(false);
        deletedUnreadNotif.getNotification().setDeletedAt(Instant.now());
        
        List<Notificationuser> notifications = Arrays.asList(unreadNotif, deletedUnreadNotif);
        
        when(notificationUserRepository.findAllById_UserId(anyInt()))
            .thenReturn(notifications);

        // When
        Long count = notificationUserService.countUnreadByUserId(userId);

        // Then
        assertThat(count).isEqualTo(1); // Only non-deleted unread notification
    }

    // Helper method
    private Notificationuser createNotificationUser(Integer notificationId, Integer userId, boolean isRead) {
        Notification notif = new Notification();
        notif.setId(notificationId);
        notif.setTitle("Test Notification " + notificationId);
        notif.setMessage("Test message");
        notif.setCreatedAt(Instant.now());
        
        NotificationuserId id = new NotificationuserId();
        id.setNotificationId(notificationId);
        id.setUserId(userId);
        
        Notificationuser nu = new Notificationuser();
        nu.setId(id);
        nu.setNotification(notif);
        nu.setIsRead(isRead);
        nu.setHidden(false);

        if (isRead) {
            nu.setReadAt(Instant.now());
        }
        
        return nu;
    }
}
