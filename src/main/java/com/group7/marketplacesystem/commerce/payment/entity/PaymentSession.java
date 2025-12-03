package com.group7.marketplacesystem.commerce.payment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "payment_session", uniqueConstraints = {
        @UniqueConstraint(name = "txn_ref", columnNames = {"txn_ref"})
})
public class PaymentSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "txn_ref", nullable = false, length = 50)
    private String txnRef;

    @Column(name = "buyer_id", nullable = true)
    private Integer buyerId;


    @Column(name = "cart_id", nullable = true)
    private Integer cartId;

    @Column(name = "address_id", nullable = true)
    private Integer addressId;

    @Lob
    @Column(name = "promotions", columnDefinition = "TEXT")
    private String promotions;

    @Size(max = 500)
    @Column(name = "note", length = 500)
    private String note;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Size(max = 20)
    @NotNull
    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    @Size(max = 20)
    @ColumnDefault("'PENDING'")
    @Column(name = "status", length = 20)
    private String status;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Size(max = 50)
    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id")
    private Integer targetId;

    @Column(name = "seller_id")
    private Integer sellerId;

}