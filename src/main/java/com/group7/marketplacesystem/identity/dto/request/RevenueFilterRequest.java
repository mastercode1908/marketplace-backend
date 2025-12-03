package com.group7.marketplacesystem.identity.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueFilterRequest {
    // Filter theo thời gian
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Filter theo period type: daily, monthly, quarterly, yearly
    private String periodType; // "daily", "monthly", "quarterly", "yearly"
    
    // Filter theo seller
    private Integer sellerId;
    
    // Filter theo category
    private Integer categoryId;
    
    // Số lượng top sellers muốn lấy
    private Integer topSellersLimit;
    
    // Sắp xếp
    private String sortBy; // "revenue", "orders", "commission"
    private String sortDirection; // "asc", "desc"
}



