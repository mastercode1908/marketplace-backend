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
@Table(name = "wishlist")
public class Wishlist {
    @EmbeddedId
    private WishlistId id;

    @MapsId("buyerId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @MapsId("productId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "added_at")
    private Instant addedAt;

}