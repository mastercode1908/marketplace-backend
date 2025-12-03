package com.group7.marketplacesystem.commerce.shipping.controller;

import com.group7.marketplacesystem.commerce.shipping.dto.ghn.*;
import com.group7.marketplacesystem.commerce.shipping.dto.request.BuyerAddressRequest;
import com.group7.marketplacesystem.commerce.shipping.dto.request.GHNShopInfoPreviewRequest;
import com.group7.marketplacesystem.commerce.shipping.dto.request.GHNShopInfoRequest;
import com.group7.marketplacesystem.commerce.shipping.dto.response.BuyerAddressResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.CheckoutResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.GHNShopInfoPreviewResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.GHNShopInfoResponse;
import com.group7.marketplacesystem.commerce.shipping.service.ShippingService;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho shipping và địa chỉ
 */
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {
    private final ShippingService shippingService;

    /**
     * Lấy danh sách tỉnh/thành phố từ GHN
     * Endpoint công khai, không cần authentication
     */
    @GetMapping("/provinces")
    public ResponseEntity<?> getProvinces() {
        try {
            List<GHNProvince> provinces = shippingService.getProvinces();
            return ResponseEntity.ok(provinces);
        } catch (Exception e) {
            e.printStackTrace();
            // Trả về lỗi để frontend biết và xử lý
            return ResponseEntity.status(500).body(
                java.util.Map.of(
                    "error", "Failed to get provinces from GHN",
                    "message", e.getMessage()
                )
            );
        }
    }

    /**
     * Lấy danh sách quận/huyện theo tỉnh
     */
    @GetMapping("/districts")
    public ResponseEntity<List<GHNDistrict>> getDistricts(@RequestParam Integer provinceId) {
        return ResponseEntity.ok(shippingService.getDistricts(provinceId));
    }

    /**
     * Lấy danh sách phường/xã theo quận
     */
    @GetMapping("/wards")
    public ResponseEntity<List<GHNWard>> getWards(@RequestParam Integer districtId) {
        return ResponseEntity.ok(shippingService.getWards(districtId));
    }

    /**
     * Tạo địa chỉ người nhận
     */
    @PreAuthorize("hasRole('BUYER')")
    @PostMapping("/addresses")
    public ResponseEntity<BuyerAddressResponse> createAddress(@Valid @RequestBody BuyerAddressRequest request) {
        Integer buyerId = getCurrentBuyerId();
        return ResponseEntity.ok(shippingService.createAddress(buyerId, request));
    }

    /**
     * Lấy danh sách địa chỉ của buyer
     */
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/addresses")
    public ResponseEntity<List<BuyerAddressResponse>> getAddresses() {
        Integer buyerId = getCurrentBuyerId();
        return ResponseEntity.ok(shippingService.getAddressesByBuyerId(buyerId));
    }

    /**
     * Lấy địa chỉ theo ID
     */
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<BuyerAddressResponse> getAddress(@PathVariable Integer addressId) {
        Integer buyerId = getCurrentBuyerId();
        return ResponseEntity.ok(shippingService.getAddressById(addressId, buyerId));
    }

    /**
     * Xóa địa chỉ người nhận
     */
    @PreAuthorize("hasRole('BUYER')")
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Integer addressId) {
        Integer buyerId = getCurrentBuyerId();
        shippingService.deleteAddress(buyerId, addressId);
        return ResponseEntity.ok().build();
    }

    /**
     * Chuẩn bị checkout - hiển thị đơn hàng theo seller
     */
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/checkout/prepare")
    public ResponseEntity<CheckoutResponse> prepareCheckout(@RequestParam Integer addressId) {
        Integer buyerId = getCurrentBuyerId();
        return ResponseEntity.ok(shippingService.prepareCheckout(buyerId, addressId));
    }

    /**
     * Seller nhập/cập nhật thông tin GHN shop
     * Tự động lấy thông tin shop từ GHN API và lưu vào database
     */
    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/ghn-shop-info")
    public ResponseEntity<GHNShopInfoResponse> saveOrUpdateGHNShopInfo(@Valid @RequestBody GHNShopInfoRequest request) {
        Integer sellerId = getCurrentSellerId();
        return ResponseEntity.ok(shippingService.saveOrUpdateGHNShopInfo(sellerId, request));
    }

    /**
     * Lấy thông tin GHN shop của seller hiện tại
     */
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/ghn-shop-info")
    public ResponseEntity<GHNShopInfoResponse> getGHNShopInfo() {
        Integer sellerId = getCurrentSellerId();
        return ResponseEntity.ok(shippingService.getGHNShopInfo(sellerId));
    }

    /**
     * Preview shop info từ GHN (không lưu vào DB)
     * Dùng để lấy thông tin shop từ GHN và tự động điền vào form
     */
    /**
     * Tính phí vận chuyển cho sản phẩm
     */
    @PostMapping("/calculate-fee")
    public ResponseEntity<java.math.BigDecimal> calculateFee(@Valid @RequestBody com.group7.marketplacesystem.commerce.shipping.dto.request.CalculateFeeRequest request) {
        return ResponseEntity.ok(shippingService.calculateFee(request));
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/ghn-shop-info/preview")
    public ResponseEntity<GHNShopInfoPreviewResponse> previewGHNShopInfo(@Valid @RequestBody GHNShopInfoPreviewRequest request) {
        return ResponseEntity.ok(shippingService.previewGHNShopInfo(request.getGhnToken(), request.getGhnShopCode()));
    }

    private Integer getCurrentBuyerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }

    private Integer getCurrentSellerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }
}

