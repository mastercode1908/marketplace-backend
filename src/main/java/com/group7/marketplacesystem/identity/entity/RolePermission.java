package com.group7.marketplacesystem.identity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "rolepermissions", indexes = {
        @Index(name = "permission_id", columnList = "permission_id")
})
public class RolePermission {
    @EmbeddedId
    private RolePermissionId id;

    @MapsId("permissionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}