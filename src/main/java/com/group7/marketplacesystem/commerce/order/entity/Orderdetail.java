package com.group7.marketplacesystem.commerce.order.entity;

import com.group7.marketplacesystem.catalog.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "orderdetail", indexes = {
        @Index(name = "idx_od_order", columnList = "order_id"),
        @Index(name = "idx_od_product", columnList = "product_id")
})
public class Orderdetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @ColumnDefault("(`quantity` * `unit_price`)")
    @Column(name = "subtotal", precision = 14, scale = 2, insertable = false, updatable = false)
    private BigDecimal subtotal;

    @Column(name = "is_reviewed")
    private Boolean isReviewed = false;

    @Column(name = "is_reported")
    private Boolean isReported = false;

}