package com.group7.marketplacesystem.communication.entity;

import com.group7.marketplacesystem.identity.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "notificationuser")
public class Notificationuser {
    @EmbeddedId
    private NotificationuserId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("notificationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ColumnDefault("0")
    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "read_at")
    private Instant readAt;


    //Ẩn thông báo
    @ColumnDefault("0")
    @Column(name = "hidden")
    private Boolean hidden;

}