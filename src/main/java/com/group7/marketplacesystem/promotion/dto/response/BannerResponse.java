package com.group7.marketplacesystem.promotion.dto.response;
import com.group7.marketplacesystem.promotion.entity.Banner.BannerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerResponse {
    private Integer bannerId;
    private String shopName;
    private Integer sellerId;
    private String title;
    private String description;
    private String imageUrl;
    private Integer position;
    private Integer priority;
    private Instant startDate;
    private Instant endDate;
    private BannerStatus status;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
}
