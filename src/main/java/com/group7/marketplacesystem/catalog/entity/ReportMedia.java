package com.group7.marketplacesystem.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "report_media")
public class ReportMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "url")
    private String url;

    @Column(name = "public_id")
    private String publicId;

    @Column(name = "media_type")
    private String mediaType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Productreport report;
}
