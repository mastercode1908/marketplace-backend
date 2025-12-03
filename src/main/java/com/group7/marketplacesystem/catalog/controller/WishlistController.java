package com.group7.marketplacesystem.catalog.controller;

import com.group7.marketplacesystem.catalog.dto.request.WishlistRequest;
import com.group7.marketplacesystem.catalog.dto.response.WishlistResponse;
import com.group7.marketplacesystem.catalog.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @PreAuthorize("hasRole('BUYER')")
    @PostMapping("/product/{product_id}")
    public ResponseEntity<String> createWishlist(
            @PathVariable("product_id") Long productId,
            @RequestBody WishlistRequest wishlistRequest) {

        wishlistRequest.setProductId(Integer.parseInt(productId.toString()));

        String message = wishlistService.creatWishlist(wishlistRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PreAuthorize("hasRole('BUYER')")
    @DeleteMapping("/{product_id}/delete")
    public ResponseEntity<String> deleteWishlist(@PathVariable("product_id") Long productId) {
        String message = wishlistService.deleteWishlist(productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PreAuthorize("hasRole('BUYER')")
    @GetMapping
    public ResponseEntity<Page<WishlistResponse>> getAllWishlist(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "2") Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<WishlistResponse> wishlists = wishlistService.getAllWishlists(pageable);

        return ResponseEntity.ok(wishlists);
    }

    @PreAuthorize("hasRole('BUYER')")
    @PostMapping("/search")
    public ResponseEntity<List<WishlistResponse>> getWishlist(
            @RequestBody WishlistRequest wishlistRequest) {

        List<WishlistResponse> wishlists = wishlistService.searchWishlist(wishlistRequest.getText());
        return ResponseEntity.ok(wishlists);
    }
}
