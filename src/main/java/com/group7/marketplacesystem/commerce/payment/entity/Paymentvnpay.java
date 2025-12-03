package com.group7.marketplacesystem.commerce.payment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "paymentvnpay")
public class Paymentvnpay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @Column(name = "vnp_transaction_code", length = 255)
    private String vnpTransactionCode;

    @Size(max = 10)
    @Column(name = "vnp_response_code", length = 10)
    private String vnpResponseCode;

    @Size(max = 20)
    @Column(name = "vnp_bank_code", length = 20)
    private String vnpBankCode;

    @Column(name = "vnp_pay_date")
    private Instant vnpPayDate;

    @Size(max = 2000)
    @Column(name = "vnp_order_info")
    private String vnpOrderInfo;

    @ColumnDefault("'Pending'")
    @Column(name = "status")
    private String status;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Size(max = 255)
    @NotNull
    @Column(name = "vnp_txn_ref", nullable = false)
    private String vnpTxnRef;

}