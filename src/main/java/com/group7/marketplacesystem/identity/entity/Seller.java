package com.group7.marketplacesystem.identity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "seller", indexes = {
        @Index(name = "idx_seller_shop_name", columnList = "shop_name")
}, uniqueConstraints = {
        @UniqueConstraint(name = "shop_name", columnNames = {"shop_name"}),
        @UniqueConstraint(name = "tax_code", columnNames = {"tax_code"})
})
public class Seller {
    @Id
    @Column(name = "seller_id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User users;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "shop_address", length = 500)
    private String shopAddress;

    @Lob
    @Column(name = "shop_description")
    private String shopDescription;

    @Column(name = "tax_code", length = 100)
    private String taxCode;

    @ColumnDefault("0")
    @Column(name = "rating_count")
    private Integer ratingCount;

    @ColumnDefault("0")
    @Column(name = "total_sales")
    private Integer totalSales;
}