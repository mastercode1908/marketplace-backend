package com.group7.marketplacesystem.promotion.mapper;

import com.group7.marketplacesystem.identity.entity.Admin;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.promotion.dto.request.PromotionRequest;
import com.group7.marketplacesystem.promotion.dto.response.PromotionResponse;
import com.group7.marketplacesystem.promotion.entity.Promotion;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class PromotionMapper {

    // chuyển từ PromotionRequest thành entity Promotion
    public Promotion toEntity(PromotionRequest request, User user) {
        if (request == null) {
            return null;
        }

        Promotion promotion = new Promotion();
        promotion.setPromotionCode(request.getPromotionCode().trim());
        promotion.setDescription(request.getDescription().trim());
        promotion.setDiscountType(request.getDiscountType());
        promotion.setUsageLimit(request.getUsageLimit());
        promotion.setUsedCount(0);
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setPromotionStatus(request.getPromotionStatus() != null ? request.getPromotionStatus() : "Active");

        promotion.setOwnerType(user.getRole());
        promotion.setOwnerId(user.getId());
        
        promotion.setCreatedAt(Instant.now());
        promotion.setUpdatedAt(Instant.now());

        return promotion;
    }

    // Map từ entity Promotion sang PromotionResponse
    public PromotionResponse toResponse(Promotion promotion) {
        if (promotion == null) {
            return null;
        }

        return PromotionResponse.builder()
                .id(promotion.getId())
                .ownerType(promotion.getOwnerType())
                .ownerId(promotion.getOwnerId())
                .promotionCode(promotion.getPromotionCode().trim())
                .description(promotion.getDescription().trim())
                .discountType(promotion.getDiscountType())
                .usageLimit(promotion.getUsageLimit())
                .usedCount(promotion.getUsedCount())
                .discountValue(promotion.getDiscountValue())
                .maxDiscountAmount(promotion.getMaxDiscountAmount())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .promotionStatus(promotion.getPromotionStatus())
                .createdAt(promotion.getCreatedAt())
                .updatedAt(promotion.getUpdatedAt())
                .build();
    }

    public void updateEntity(Promotion promotion, PromotionRequest request) {
        if (promotion == null || request == null) {
            return;
        }

        promotion.setPromotionCode(request.getPromotionCode().trim());
        promotion.setDescription(request.getDescription());
        promotion.setDiscountType(request.getDiscountType());
        promotion.setUsageLimit(request.getUsageLimit());
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        if (request.getPromotionStatus() != null) {
            promotion.setPromotionStatus(request.getPromotionStatus());
        }
        promotion.setUpdatedAt(Instant.now());
    }
}
