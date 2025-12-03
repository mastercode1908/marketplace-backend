package com.group7.marketplacesystem.identity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "userprovider", indexes = {
        @Index(name = "user_id", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "provider", columnNames = {"provider", "provider_id"})
})
public class UserProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "linked_at")
    private Instant linkedAt;

}