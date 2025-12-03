package com.group7.marketplacesystem.catalog.entity;

import com.group7.marketplacesystem.identity.entity.Buyer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "review", indexes = {
        @Index(name = "idx_review_product", columnList = "product_id"),
        @Index(name = "idx_review_buyer", columnList = "buyer_id"),
        @Index(name = "idx_review_rating", columnList = "rating")
})
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @ColumnDefault("0")
    @Column(name = "is_verified_purchase")
    private Boolean isVerifiedPurchase;

    @Lob
    @Column(name = "comment")
    private String comment;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}