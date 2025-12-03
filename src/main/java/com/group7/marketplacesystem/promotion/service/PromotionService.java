package com.group7.marketplacesystem.promotion.service;

import com.group7.marketplacesystem.promotion.dto.request.PromotionApplyRequest;
import com.group7.marketplacesystem.promotion.dto.request.PromotionRequest;
import com.group7.marketplacesystem.promotion.dto.response.PromotionApplyResponse;
import com.group7.marketplacesystem.promotion.dto.response.PromotionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PromotionService {
    PromotionResponse createPromotion(PromotionRequest request, Integer adminId);
    PromotionResponse getPromotionById(Integer id);
    Page<PromotionResponse> getAllPromotions(Pageable pageable);
    Page<PromotionResponse> getPromotionsByIdUser(Pageable pageable, Integer userId);
    PromotionResponse updatePromotion(Integer id, PromotionRequest request);
    void deletePromotion(Integer id);
    void expirePromotions();


    PromotionApplyResponse previewPromotion(PromotionApplyRequest request);
    void savePromotionUsage(Integer promotionId, Integer buyerId, Integer orderId);

    // Seller promotion methods
    PromotionResponse createSellerPromotion(PromotionRequest request, Integer sellerId);
    PromotionResponse updateSellerPromotion(Integer id, PromotionRequest request, Integer sellerId);
    void deleteSellerPromotion(Integer id, Integer sellerId);
    Page<PromotionResponse> getSellerPromotions(Pageable pageable, Integer sellerId);

}
