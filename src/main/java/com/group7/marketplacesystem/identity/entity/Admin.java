package com.group7.marketplacesystem.identity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "admin")
public class Admin {
    @Id
    @Column(name = "admin_id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private User users;

    @Column(name = "department")
    private String department;

    @Column(name = "position")
    private String position;

    @Lob
    @Column(name = "note")
    private String note;
}