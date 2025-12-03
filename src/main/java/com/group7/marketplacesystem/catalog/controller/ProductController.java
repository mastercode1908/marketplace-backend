package com.group7.marketplacesystem.catalog.controller;

import com.group7.marketplacesystem.catalog.dto.request.ProductCreateRequest;
import com.group7.marketplacesystem.catalog.dto.response.ProductInfoResponse;
import com.group7.marketplacesystem.catalog.service.ProductService;
import com.group7.marketplacesystem.common.constants.MessageConstants;
import com.group7.marketplacesystem.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PRODUCT') or hasAuthority('ROLE_SELLER')")
    public ResponseEntity<ApiResponse<ProductInfoResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        ProductInfoResponse response = productService.createProduct(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCT_CREATED, response));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductInfoResponse>> getProductById(
            @PathVariable Integer productId) {
        ProductInfoResponse response = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('UPDATE_PRODUCT') or hasAuthority('ROLE_SELLER')")
    public ResponseEntity<ApiResponse<ProductInfoResponse>> updateProduct(
            @Valid @PathVariable Integer productId,
            @RequestBody ProductCreateRequest request) {
        ProductInfoResponse response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCT_UPDATED, response));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('DELETE_PRODUCT') or hasAuthority('ROLE_SELLER')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Integer productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PRODUCT_DELETED, null));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('VIEW_PRODUCT') or hasAuthority('ROLE_SELLER')")
    public ResponseEntity<ApiResponse<List<ProductInfoResponse>>> getMyProducts() {
        List<ProductInfoResponse> responses = productService.getMyProducts();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductInfoResponse>>> getAllProducts() {
        List<ProductInfoResponse> responses = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // @PreAuthorize("hasAuthority('VIEW_PRODUCT')")
    @GetMapping("/all")
    public ResponseEntity<Page<ProductInfoResponse>> getAllProduct(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<ProductInfoResponse> responses = productService.getAllProductsRandomized(pageable);

        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasAuthority('VIEW_PRODUCT')")
    @GetMapping("/priority")
    public ResponseEntity<Page<ProductInfoResponse>> getAllProductsOfShopHasServicePackage(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<ProductInfoResponse> responses = productService.getAllProductsOfShopHasServicePackage(pageable);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/promoted")
    public ResponseEntity<ApiResponse<List<ProductInfoResponse>>> getPromotedProducts() {
        List<ProductInfoResponse> responses = productService.getPromotedProducts();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}

//
// // @PreAuthorize("hasRole('SYSTEMADMIN') or hasRole('SELLER')")
// @PreAuthorize("hasAuthority('VIEW_PRODUCT')")
// @GetMapping
// public ResponseEntity<List<ProductResponse>> getAllProduct() {
// return ResponseEntity.ok(productService.getAllProduct());
// }
