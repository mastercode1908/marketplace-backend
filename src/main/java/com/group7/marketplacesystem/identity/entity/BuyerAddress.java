package com.group7.marketplacesystem.identity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "buyeraddress", indexes = {
        @Index(name = "idx_buyeraddress_buyer", columnList = "buyer_id")
})
public class BuyerAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 50)
    private String receiverPhone;

    @Column(name = "address_detail", nullable = false, length = 500)
    private String addressDetail;

    @Column(name = "ward_code", nullable = false, length = 50)
    private String wardCode;

    @Column(name = "district_id", nullable = false)
    private Integer districtId;

    @Column(name = "province_name", length = 255)
    private String provinceName;

    @ColumnDefault("FALSE")
    @Column(name = "is_default")
    private Boolean isDefault;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}


