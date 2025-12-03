package com.group7.marketplacesystem.identity.controller;

import com.group7.marketplacesystem.common.response.ApiResponse;
import com.group7.marketplacesystem.identity.dto.request.SellerRejectRequest;
import com.group7.marketplacesystem.identity.dto.response.SellerReviewResponse;
import com.group7.marketplacesystem.identity.service.SellerReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sellers")
@RequiredArgsConstructor
public class SellerReviewController {

    private final SellerReviewService sellerReviewService;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<Page<SellerReviewResponse>> getPendingSellers(Pageable pageable) {
        return ResponseEntity.ok(sellerReviewService.getPendingSellers(pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<Page<SellerReviewResponse>> getSellers(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(sellerReviewService.getSellersByStatus(status, pageable));
    }

    @PostMapping("/{sellerId}/approve")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<ApiResponse<String>> approveSeller(@PathVariable Integer sellerId) {
        sellerReviewService.approveSeller(sellerId);
        return ResponseEntity.ok(ApiResponse.success("Đã duyệt seller thành công!", null));
    }

    @PostMapping("/{sellerId}/reject")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<ApiResponse<String>> rejectSeller(
            @PathVariable Integer sellerId,
            @Valid @RequestBody SellerRejectRequest request) {
        sellerReviewService.rejectSeller(sellerId, request);
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối seller và gửi email thông báo!", null));
    }
}

