package com.group7.marketplacesystem.commerce.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "payment", indexes = {
        @Index(name = "idx_payment_target", columnList = "target_type, target_id"),
        @Index(name = "idx_payment_status", columnList = "status")
})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Integer id;

    @Column(name = "target_id", nullable = false)
    private Integer targetId;

    @Column(name = "target_type", nullable = false, length = 100)
    private String targetType;

    @ColumnDefault("'COD'")
    @Column(name = "method")
    private String method;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @ColumnDefault("'Pending'")
    @Column(name = "status")
    private String status;

    @Column(name = "transaction_code")
    private String transactionCode;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}