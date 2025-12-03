package com.group7.marketplacesystem.commerce.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOptionDTO {
    private Integer sellerId;
    private String sellerName;
    private String sellerEmail;
}





