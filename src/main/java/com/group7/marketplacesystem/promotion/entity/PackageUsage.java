package com.group7.marketplacesystem.promotion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "package_usage", indexes = {
        @Index(name = "idx_package_usage_sp", columnList = "seller_package_id"),
        @Index(name = "idx_package_usage_target", columnList = "target_type, target_id")
})
public class PackageUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_package_id", nullable = false)
    private Sellerpackage sellerPackage;

    @Column(name = "target_id", nullable = false)
    private Integer targetId;

    @Column(name = "target_type", nullable = false, length = 100)
    private String targetType;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "used_at")
    private Instant usedAt;

}