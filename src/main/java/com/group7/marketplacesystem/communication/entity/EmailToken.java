package com.group7.marketplacesystem.communication.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "emailtoken", indexes = {
        @Index(name = "idx_entity", columnList = "entity_type, entity_id, type")
})
public class EmailToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Integer entityId;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "type", length = 20, nullable = false)
    private String type = "VERIFY";

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

}