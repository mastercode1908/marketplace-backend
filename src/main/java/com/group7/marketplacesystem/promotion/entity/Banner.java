package com.group7.marketplacesystem.promotion.entity;


import com.group7.marketplacesystem.identity.entity.Seller;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
@Getter
@Setter
@Entity
@Table(name = "Banner", indexes = {
        @Index(name = "idx_banner_status", columnList = "status"),
        @Index(name = "idx_banner_dates", columnList = "start_date, end_date"),
        @Index(name = "idx_banner_priority", columnList = "priority DESC, position DESC")
})
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;



    private String title;

    @Lob
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    private Integer position = 0;
    private Integer priority = 0;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    private BannerStatus status = BannerStatus.PENDING;

    @Lob
    private String rejectionReason;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum BannerStatus { PENDING, ACTIVE, PAUSED, COMPLETED, REJECTED }
}


