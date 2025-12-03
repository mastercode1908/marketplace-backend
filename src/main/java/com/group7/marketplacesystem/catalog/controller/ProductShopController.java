package com.group7.marketplacesystem.catalog.controller;

import com.group7.marketplacesystem.catalog.dto.response.ProductInfoResponse;
import com.group7.marketplacesystem.catalog.service.ProductService;
import com.group7.marketplacesystem.common.response.ApiResponse;
import com.group7.marketplacesystem.identity.dto.response.SellerResponse;
import com.group7.marketplacesystem.identity.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product/shop")
@RequiredArgsConstructor
public class ProductShopController {

    private final SellerService sellerService;
    private final ProductService productService;

    @GetMapping("/{sellerId}")
    public ResponseEntity<ApiResponse<SellerResponse>> getShopInfo(@PathVariable Integer sellerId) {
        SellerResponse response = sellerService.getSellerProfile(sellerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{sellerId}/products")
    public ResponseEntity<ApiResponse<List<ProductInfoResponse>>> getShopProducts(@PathVariable Integer sellerId) {
        List<ProductInfoResponse> responses = productService.getProductsBySellerId(sellerId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
