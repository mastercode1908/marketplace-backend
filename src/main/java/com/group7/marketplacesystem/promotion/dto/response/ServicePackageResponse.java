package com.group7.marketplacesystem.promotion.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ServicePackageResponse {
    private Integer id;
    private Integer adminId;
    private String name;
    private String type;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private Integer usageLimit;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}
