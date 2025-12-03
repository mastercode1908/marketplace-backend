package com.group7.marketplacesystem.commerce.order.entity;

import com.group7.marketplacesystem.promotion.entity.Promotion;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.Seller;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "`order`", indexes = {
        @Index(name = "idx_order_buyer", columnList = "buyer_id"),
        @Index(name = "idx_order_seller", columnList = "seller_id"),
        @Index(name = "idx_order_date", columnList = "order_date"),
        @Index(name = "idx_order_status", columnList = "order_status")
})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(name = "cart_id")
    private Integer cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(name = "total_amount", precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 14, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "shipping_fee", precision = 14, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "final_amount", precision = 14, scale = 2)
    private BigDecimal finalAmount;

    @Lob
    @Column(name = "note")
    private String note;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "order_date")
    private Instant orderDate;

    @ColumnDefault("'Pending'")
    @Column(name = "order_status")
    private String orderStatus;

}