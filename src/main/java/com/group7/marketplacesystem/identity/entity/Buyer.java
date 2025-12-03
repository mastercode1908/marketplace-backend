package com.group7.marketplacesystem.identity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "buyer")
public class Buyer {
    @Id
    @Column(name = "buyer_id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User users;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

}