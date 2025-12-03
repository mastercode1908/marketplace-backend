package com.group7.marketplacesystem.promotion.controller;

import com.group7.marketplacesystem.common.response.ApiResponse;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.promotion.dto.request.PromotionApplyRequest;
import com.group7.marketplacesystem.promotion.dto.request.PromotionRequest;
import com.group7.marketplacesystem.promotion.dto.response.PromotionApplyResponse;
import com.group7.marketplacesystem.promotion.dto.response.PromotionResponse;
import com.group7.marketplacesystem.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Slf4j
public class PromotionController {

    private final PromotionService promotionService;

    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @PostMapping
    public ResponseEntity<PromotionResponse> createPromotion(@Valid @RequestBody PromotionRequest request) {
        log.info("Admin creating new promotion with code: {}", request.getPromotionCode());

        Integer userId = getCurrentUserId();
        
        PromotionResponse response = promotionService.createPromotion(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getPromotionById(@PathVariable Integer id) {
        PromotionResponse response = promotionService.getPromotionById(id);
        return ResponseEntity.ok(response);
    }

//    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @GetMapping
    public ResponseEntity<Page<PromotionResponse>> getAllPromotions(Pageable pageable) {
        Page<PromotionResponse> responses = promotionService.getAllPromotions(pageable);
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/{id}/seller")
    public ResponseEntity<Page<PromotionResponse>> getPromotionsByIdUser(Pageable pageable, @PathVariable Integer id) {
        Page<PromotionResponse> responses = promotionService.getPromotionsByIdUser(pageable,  id);
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> updatePromotion(
            @PathVariable Integer id,
            @Valid @RequestBody PromotionRequest request) {

        PromotionResponse response = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePromotion(@PathVariable Integer id) {
        promotionService.deletePromotion(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Promotion deleted successfully");
        response.put("promotionId", id.toString());
        
        return ResponseEntity.ok(response);
    }


    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<PromotionApplyResponse>> previewPromotion(
            @RequestBody @Valid PromotionApplyRequest req) {

        PromotionApplyResponse response = promotionService.previewPromotion(req);

        return ResponseEntity.ok(ApiResponse.success("Preview promotion successful", response));
    }

    // ========================
    //  SELLER PROMOTION ENDPOINTS
    // ========================

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/seller")
    public ResponseEntity<PromotionResponse> createSellerPromotion(@Valid @RequestBody PromotionRequest request) {
        log.info("Seller creating new promotion with code: {}", request.getPromotionCode());

        Integer sellerId = getCurrentUserId();
        PromotionResponse response = promotionService.createSellerPromotion(request, sellerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/seller/{id}")
    public ResponseEntity<PromotionResponse> updateSellerPromotion(
            @PathVariable Integer id,
            @Valid @RequestBody PromotionRequest request) {

        Integer sellerId = getCurrentUserId();
        PromotionResponse response = promotionService.updateSellerPromotion(id, request, sellerId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/seller/{id}")
    public ResponseEntity<Map<String, String>> deleteSellerPromotion(@PathVariable Integer id) {
        Integer sellerId = getCurrentUserId();
        promotionService.deleteSellerPromotion(id, sellerId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Promotion deleted successfully");
        response.put("promotionId", id.toString());
        
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/seller/my-promotions")
    public ResponseEntity<Page<PromotionResponse>> getSellerPromotions(Pageable pageable) {
        Integer sellerId = getCurrentUserId();
        Page<PromotionResponse> responses = promotionService.getSellerPromotions(pageable, sellerId);
        return ResponseEntity.ok(responses);
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }
}
