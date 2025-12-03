package com.group7.marketplacesystem.promotion.entity;

import com.group7.marketplacesystem.identity.entity.Admin;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "servicepackage", indexes = {
        @Index(name = "idx_sp_name", columnList = "name")
})
public class Servicepackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "price", nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}