package com.group7.marketplacesystem.commerce.cart.controller;

import com.group7.marketplacesystem.commerce.cart.dto.request.AddCartItemRequest;
import com.group7.marketplacesystem.commerce.cart.dto.request.UpdateCartItemQuantityRequest;
import com.group7.marketplacesystem.commerce.cart.service.CartService;
import com.group7.marketplacesystem.commerce.shipping.dto.response.CartResponse;
import com.group7.marketplacesystem.common.constants.MessageConstants;
import com.group7.marketplacesystem.common.response.MessageResponse;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho giỏ hàng
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    /**
     * Lấy giỏ hàng của buyer hiện tại
     */
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        Integer buyerId = getCurrentBuyerId();
        return ResponseEntity.ok(cartService.getCartByBuyerId(buyerId));
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @PreAuthorize("hasRole('BUYER')")
    @PostMapping("/items")
    public ResponseEntity<MessageResponse> addCartItem(@Valid @RequestBody AddCartItemRequest request) {
        Integer buyerId = getCurrentBuyerId();
        cartService.addCartItem(buyerId, request);
        return ResponseEntity.ok(new MessageResponse(MessageConstants.CART_ADD_SUCCESS));
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    @PreAuthorize("hasRole('BUYER')")
    @PutMapping("/items/{itemId}/quantity")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @PathVariable Integer itemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        Integer buyerId = getCurrentBuyerId();
        return ResponseEntity.ok(cartService.updateCartItemQuantity(buyerId, itemId, request));
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng
     */
    @PreAuthorize("hasRole('BUYER')")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> deleteCartItem(@PathVariable Integer itemId) {
        Integer buyerId = getCurrentBuyerId();
        return ResponseEntity.ok(cartService.deleteCartItem(buyerId, itemId));
    }

    /**
     * Xóa tất cả sản phẩm khỏi giỏ hàng
     */
    @PreAuthorize("hasRole('BUYER')")
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        Integer buyerId = getCurrentBuyerId();
        cartService.clearCart(buyerId);
        return ResponseEntity.ok().build();
    }

    private Integer getCurrentBuyerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }
}


