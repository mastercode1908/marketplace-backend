package com.group7.marketplacesystem.catalog.dto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class ProductReportResponse {
    private Integer id;
    private Integer productId;
    private Integer buyerId;
    private String reason;
    private String status;
    private Instant createdAt;
    private Instant resolvedAt;
    private String shop_name;
    private Integer sellerId;
    private Integer rating_count;
    private String product_name;
    private String buyerName;
    private String productImage;
    private java.util.List<com.group7.marketplacesystem.catalog.dto.common.ReportMediaDTO> media;
}
