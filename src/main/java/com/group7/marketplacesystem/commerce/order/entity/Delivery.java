package com.group7.marketplacesystem.commerce.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "delivery", indexes = {
}, uniqueConstraints = {
        @UniqueConstraint(name = "order_id", columnNames = {"order_id"})
})
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "tracking_number", nullable = false)
    private String trackingNumber;

    @Column(name = "shipping_fee", nullable = false, precision = 14, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "estimated_date")
    private LocalDate estimatedDate;

    @Column(name = "actual_date")
    private LocalDate actualDate;

    // GHN fields
    @Column(name = "ghn_order_code", length = 100)
    private String ghnOrderCode;

    @Column(name = "ghn_service_id")
    private Integer ghnServiceId;

    @Column(name = "ghn_expected_delivery_time")
    private LocalDateTime ghnExpectedDeliveryTime;

    @Column(name = "ghn_status", length = 100)
    private String ghnStatus;

    @Column(name = "ghn_total_fee", precision = 14, scale = 2)
    private BigDecimal ghnTotalFee;

    @Column(name = "ghn_created_at")
    private LocalDateTime ghnCreatedAt;

    @Column(name = "ghn_updated_at")
    private LocalDateTime ghnUpdatedAt;

    // Address ID để lấy thông tin địa chỉ giao hàng
    @Column(name = "address_id")
    private Integer addressId;

}