package com.group7.marketplacesystem.promotion.entity;

import com.group7.marketplacesystem.catalog.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "flashsaleitem", indexes = {
        @Index(name = "idx_fsi_product", columnList = "product_id")
})
public class Flashsaleitem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flash_sale_item_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flash_sale_id", nullable = false)
    private Flashsale flashSale;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "flash_sale_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal flashSalePrice;

    @Column(name = "stock_limit", nullable = false)
    private Integer stockLimit;

    @ColumnDefault("0")
    @Column(name = "sold_quantity")
    private Integer soldQuantity;

    @ColumnDefault("'PendingApproval'")
    @Column(name = "fs_item_status")
    private String fsitemStatus;

    @ColumnDefault("0")
    @Column(name = "seller_notified")
    private Boolean sellerNotified;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

}