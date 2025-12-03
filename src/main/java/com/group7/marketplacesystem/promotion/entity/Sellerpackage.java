package com.group7.marketplacesystem.promotion.entity;

import com.group7.marketplacesystem.identity.entity.Seller;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "sellerpackage", indexes = {
        @Index(name = "idx_sp_seller", columnList = "seller_id"),
        @Index(name = "idx_sp_status", columnList = "status")
})
public class Sellerpackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_package_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private Servicepackage packageField;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "remaining_usage")
    private Integer remainingUsage;

    @ColumnDefault("'Active'")
    @Column(name = "status")
    private String status;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

}