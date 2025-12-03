package com.group7.marketplacesystem.promotion.entity;

import com.group7.marketplacesystem.identity.entity.Admin;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "promotion", indexes = {
        @Index(name = "idx_promotion_code", columnList = "promotion_code"),
        @Index(name = "idx_promotion_dates", columnList = "start_date, end_date"),
        @Index(name = "idx_promotion_status", columnList = "promotion_status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "promotion_code", columnNames = {"promotion_code"})
})
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id", nullable = false)
    private Integer id;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "admin_id", nullable = false)
//    private Admin admin;

    @NotNull
    @ColumnDefault("'SYSTEMADMIN'")
    @Column(name = "owner_type", nullable = false)
    private String ownerType;

    @NotNull
    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Column(name = "promotion_code", nullable = false, length = 100)
    private String promotionCode;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @ColumnDefault("'PERCENT'")
    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @ColumnDefault("0")
    @Column(name = "used_count")
    private Integer usedCount;

    @Column(name = "discount_value", nullable = false, precision = 14, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "max_discount_amount", precision = 14, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ColumnDefault("'Active'")
    @Column(name = "promotion_status")
    private String promotionStatus;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}