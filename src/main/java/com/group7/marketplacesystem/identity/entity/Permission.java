package com.group7.marketplacesystem.identity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permissions_deleted_at", columnList = "deleted_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "code", columnNames = {"code"})
})
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id", nullable = false)
    private Integer id;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}