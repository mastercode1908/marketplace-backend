package com.group7.marketplacesystem.promotion.mapper;

import com.group7.marketplacesystem.promotion.dto.response.SellerPackageResponse;
import com.group7.marketplacesystem.promotion.entity.Sellerpackage;
import org.springframework.stereotype.Component;

@Component
public class SellerPackageMapper {

    public SellerPackageResponse toResponse(Sellerpackage sellerpackage) {
        if (sellerpackage == null) {
            return null;
        }
        SellerPackageResponse response = new SellerPackageResponse();
        response.setId(sellerpackage.getId());
        response.setSellerId(sellerpackage.getSeller().getId());
        response.setPackageId(sellerpackage.getPackageField().getId());
        response.setStartDate(sellerpackage.getStartDate());
        response.setEndDate(sellerpackage.getEndDate());
        response.setRemainingUsage(sellerpackage.getRemainingUsage());
        response.setStatus(sellerpackage.getStatus());
        response.setCreatedAt(sellerpackage.getCreatedAt());
        response.setPackageName(sellerpackage.getPackageField().getName());
        return response;
    }
}
