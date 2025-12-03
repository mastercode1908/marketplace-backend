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
@Table(name = "productreport", indexes = {
        @Index(name = "idx_pr_product", columnList = "product_id"),
        @Index(name = "idx_pr_status", columnList = "status")
})
public class Productreport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @ColumnDefault("'Pending'")
    @Column(name = "status")
    private String status;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ReportMedia> media = new java.util.ArrayList<>();

}