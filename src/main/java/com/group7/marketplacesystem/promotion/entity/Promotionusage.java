package com.group7.marketplacesystem.promotion.entity;

import com.group7.marketplacesystem.commerce.order.entity.Order;
import com.group7.marketplacesystem.identity.entity.Buyer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "promotionusage", indexes = {
        @Index(name = "idx_promotion_buyer", columnList = "promotion_id, buyer_id"),
        @Index(name = "idx_promotion", columnList = "promotion_id"),
        @Index(name = "idx_buyer", columnList = "buyer_id")
})
public class Promotionusage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "used_at")
    private Instant usedAt;

}