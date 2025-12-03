package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsResponse {
    private long totalReviews;      // Tổng số review
    private double averageRating;   // Điểm trung bình
    private long oneStar;
    private long twoStar;
    private long threeStar;
    private long fourStar;
    private long fiveStar;
}
