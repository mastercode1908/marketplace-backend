package com.group7.marketplacesystem.catalog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "productmedia")
public class Productmedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Size(max = 500)
    @NotNull
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @NotNull
    @Column(name = "public_id", nullable = false)
    private String public_id;

    @NotNull
    @Lob
    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @ColumnDefault("0")
    @Column(name = "position")
    private Integer position;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}