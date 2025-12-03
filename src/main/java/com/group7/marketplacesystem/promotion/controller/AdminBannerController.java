package com.group7.marketplacesystem.promotion.controller;

import com.group7.marketplacesystem.promotion.dto.response.BannerResponse;
import com.group7.marketplacesystem.promotion.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    @PreAuthorize("hasRole('CONTENTADMIN')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<BannerResponse> approveBanner(@PathVariable Integer id) {
        BannerResponse response = bannerService.approveBanner(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CONTENTADMIN')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<BannerResponse> rejectBanner(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        String rejectionReason = payload.get("rejectionReason");
        BannerResponse response = bannerService.rejectBanner(id, rejectionReason);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CONTENTADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<BannerResponse> getBannerById(@PathVariable Integer id) {
        BannerResponse response = bannerService.getBannerById(id);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('CONTENTADMIN')")
    @GetMapping
    public ResponseEntity<Page<BannerResponse>> getAllBanners(Pageable pageable) {
        Page<BannerResponse> response = bannerService.getAllBanners(pageable);
        return ResponseEntity.ok(response);
    }
}
