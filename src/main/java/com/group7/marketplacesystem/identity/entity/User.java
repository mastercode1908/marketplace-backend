package com.group7.marketplacesystem.identity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_role", columnList = "role")
}, uniqueConstraints = {
        @UniqueConstraint(name = "username", columnNames = {"username"}),
        @UniqueConstraint(name = "email", columnNames = {"email"}),
        @UniqueConstraint(name = "phone", columnNames = {"phone"})
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "password", nullable = true)
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "full_name", nullable = true)
    private String fullName;

    @Column(name = "gender")
    private Boolean gender;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @ColumnDefault("'BUYER'")
    @Column(name = "role")
    private String role;

    @ColumnDefault("'Pending'")
    @Column(name = "user_status")
    private String userStatus;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}