package com.group7.marketplacesystem.infrastructure.entity;

import com.group7.marketplacesystem.identity.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "systemlog", indexes = {
        @Index(name = "idx_systemlog_user", columnList = "user_id"),
        @Index(name = "idx_systemlog_action", columnList = "action")
})
public class Systemlog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "action", nullable = false)
    private String action;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

}