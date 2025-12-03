package com.group7.marketplacesystem.commerce.shipping.entity;

import com.group7.marketplacesystem.identity.entity.Seller;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/**
 * Entity lưu thông tin shop GHN của seller
 * Tạm thời dùng token và shopId chung cho tất cả seller
 */
@Getter
@Setter
@Entity
@Table(name = "ghnshopinfo", indexes = {
        @Index(name = "idx_ghnshopinfo_seller", columnList = "seller_id")
})
public class GHNShopInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ghn_shop_id", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(name = "ghn_token", nullable = false, length = 255)
    private String ghnToken;

    @Column(name = "ghn_shop_code", nullable = false)
    private Integer ghnShopCode;

    @Column(name = "ghn_shop_name", length = 255)
    private String ghnShopName;

    @Column(name = "pickup_address", length = 500)
    private String pickupAddress;

    @Column(name = "pickup_district_id")
    private Integer pickupDistrictId;

    @Column(name = "pickup_ward_code", length = 50)
    private String pickupWardCode;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;
}













