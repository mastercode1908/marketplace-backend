package com.group7.marketplacesystem.promotion.controller;

import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.promotion.dto.request.CreateBannerRequest;
import com.group7.marketplacesystem.promotion.dto.response.BannerResponse;
import com.group7.marketplacesystem.promotion.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/banners")
@RequiredArgsConstructor
public class SellerBannerController {

    private final BannerService bannerService;

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public ResponseEntity<BannerResponse> createBanner(@Valid @RequestBody CreateBannerRequest request) {
        Integer sellerId = getCurrentSellerId();
        BannerResponse response = bannerService.createBanner(sellerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping
    public ResponseEntity<Page<BannerResponse>> getMyBanners(Pageable pageable) {
        Integer sellerId = getCurrentSellerId();
        Page<BannerResponse> response = bannerService.getSellerBanners(sellerId, pageable);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/{id}")
    public ResponseEntity<BannerResponse> getBannerById(@PathVariable Integer id) {
        BannerResponse response = bannerService.getBannerById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{id}")
    public ResponseEntity<BannerResponse> updateBanner(
            @PathVariable Integer id,
            @Valid @RequestBody CreateBannerRequest request) {
        Integer sellerId = getCurrentSellerId();
        BannerResponse response = bannerService.updateBanner(sellerId, id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBanner(@PathVariable Integer id) {
        Integer sellerId = getCurrentSellerId();
        bannerService.deleteBanner(sellerId, id);

        return ResponseEntity.ok("Đã xóa banner " +id);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/{id}/pause")
    public ResponseEntity<BannerResponse> pauseBanner(@PathVariable Integer id) {
        Integer sellerId = getCurrentSellerId();
        BannerResponse response = bannerService.pauseBanner(sellerId, id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/{id}/resume")
    public ResponseEntity<BannerResponse> resumeBanner(@PathVariable Integer id) {
        Integer sellerId = getCurrentSellerId();
        BannerResponse response = bannerService.resumeBanner(sellerId, id);
        return ResponseEntity.ok(response);
    }

    private Integer getCurrentSellerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }
}
