package com.group7.marketplacesystem.communication.repository;

import com.group7.marketplacesystem.communication.dto.response.NotificationUserResponse;
import com.group7.marketplacesystem.communication.entity.Notificationuser;
import com.group7.marketplacesystem.communication.entity.NotificationuserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationUserRepository extends JpaRepository<Notificationuser, NotificationuserId> {
    List<Notificationuser> findByUser_Id(Integer userId);
    boolean existsByIdUserIdAndIdNotificationId(Integer userId, Integer notificationId);

    List<Notificationuser> id(NotificationuserId id);

    List<Notificationuser> findAllById_UserId(Integer userId);


    @Query(value = "SELECT n.notification_id as id, n.title, n.message, " +
            "n.created_at as createdAt, nu.is_read as isRead " +
            "FROM notificationuser nu " +
            "JOIN notification n ON nu.notification_id = n.notification_id " +
            "WHERE nu.user_id = :userId AND nu.notification_id = :notificationId " +
            "AND n.deleted_at is null",
            nativeQuery = true)
    NotificationUserResponse getNotification(Integer notificationId, Integer userId);

    // Count how many users received this notification
    long countById_NotificationId(Integer notificationId);

    // Get list of users who received this notification
    @Query("SELECT nu FROM Notificationuser nu WHERE nu.id.notificationId = :notificationId")
    List<Notificationuser> findByNotificationId(Integer notificationId);

}
