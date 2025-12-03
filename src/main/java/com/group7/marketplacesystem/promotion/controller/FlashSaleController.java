package com.group7.marketplacesystem.promotion.controller;

import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.promotion.dto.request.FlashSaleRequest;
import com.group7.marketplacesystem.promotion.dto.response.FlashSaleResponse;
import com.group7.marketplacesystem.promotion.service.FlashSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.List;

@RestController
@RequestMapping("/api/admin/flashsale")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @PostMapping
    public ResponseEntity<FlashSaleResponse> createFlashSale(@Valid @RequestBody FlashSaleRequest request) {
        Integer adminId = getCurrentAdminId();
        FlashSaleResponse response = flashSaleService.createFlashSale(request, adminId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @GetMapping
    public ResponseEntity<List<FlashSaleResponse>> getAllFlashSale() {
        List<FlashSaleResponse> response = flashSaleService.getAllFlashSale();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<FlashSaleResponse> getFlashSaleById(@PathVariable Integer id) {
        FlashSaleResponse response = flashSaleService.getFlashSaleById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @PutMapping("/{id}/update")
    public ResponseEntity<FlashSaleResponse> updateFlashSale(@Valid @RequestBody FlashSaleRequest request, @PathVariable Integer id) {
        FlashSaleResponse response = flashSaleService.updateFlashSale(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SYSTEMADMIN')")
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteFlashSale(@PathVariable Integer id) {
        flashSaleService.deleteFlashSale(id);
        return ResponseEntity.ok("Đã xóa flash sale " + id);
    }

    private Integer getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }

}
