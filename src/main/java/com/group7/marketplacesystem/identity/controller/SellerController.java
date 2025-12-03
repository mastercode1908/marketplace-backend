package com.group7.marketplacesystem.identity.controller;

import com.group7.marketplacesystem.common.response.ApiResponse;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.identity.dto.request.SellerRequest;
import com.group7.marketplacesystem.identity.dto.request.ShopInfoRequest;
import com.group7.marketplacesystem.identity.dto.response.SellerResponse;
import com.group7.marketplacesystem.identity.dto.response.ShopInfoResponse;
import com.group7.marketplacesystem.identity.service.SellerService;
import com.group7.marketplacesystem.identity.service.ShopInfoService;
import com.group7.marketplacesystem.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static com.group7.marketplacesystem.common.constants.MessageConstants.SHOP_INFO_ADD_SUCCESS;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;
    private final ShopInfoService shopInfoService;

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/profile")
    public ResponseEntity<SellerResponse> getProfile() {
        Integer id = getCurrentSellerId();
        SellerResponse response = sellerService.getSellerProfile(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/profile")
    public ResponseEntity<SellerResponse> updateProfile(
            @Valid @RequestBody SellerRequest request) {
        Integer id = getCurrentSellerId();
        SellerResponse response = sellerService.updateProfileSeller(id, request);
        return ResponseEntity.ok(response);
    }

    private Integer getCurrentSellerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }

    @PostMapping("/shop-info")
    public ResponseEntity<ApiResponse<Void>> updateShopInfo(
            @RequestBody @Valid ShopInfoRequest request
    ) {
        shopInfoService.postShopInfo(request);
        return ResponseEntity.ok(ApiResponse.success(SHOP_INFO_ADD_SUCCESS, null));
    }

}
