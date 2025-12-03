package com.group7.marketplacesystem.promotion.controller;

import com.group7.marketplacesystem.commerce.payment.response.VNPayUrlResponse;
import com.group7.marketplacesystem.promotion.dto.response.SellerPackageResponse;
import com.group7.marketplacesystem.promotion.dto.response.ServicePackageResponse;
import com.group7.marketplacesystem.promotion.service.ServicePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/service-packages")
@RequiredArgsConstructor
public class SellerServicePackageController {

    private final ServicePackageService servicePackageService;

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping
    public ResponseEntity<Page<ServicePackageResponse>> getAllServicePackages(Pageable pageable) {
        Page<ServicePackageResponse> responsePage = servicePackageService.getAllServicePackages(pageable);
        return ResponseEntity.ok(responsePage);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/{servicePackage_id}")
    public ResponseEntity<?> buyServicePackage(
            @PathVariable("servicePackage_id") Integer id) {
        VNPayUrlResponse response = servicePackageService.buyServicePackageVNPAY(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/my-package")
    public ResponseEntity<List<SellerPackageResponse>> getAllMyServicePackages() {
        List<SellerPackageResponse> response = servicePackageService.getAllMyServicePackage();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/package-usage/{product_id}")
    public String createPackageUsage(
            @PathVariable("product_id") Integer id) {
        return servicePackageService.createPackageUsageProduct(id);
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/promoted-products")
    public ResponseEntity<List<Integer>> getPromotedProductIds() {
        List<Integer> productIds = servicePackageService.getPromotedProductIds();
        return ResponseEntity.ok(productIds);
    }
}
