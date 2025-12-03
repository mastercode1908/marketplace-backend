package com.group7.marketplacesystem.catalog.service;

import com.group7.marketplacesystem.catalog.dto.request.WishlistRequest;
import com.group7.marketplacesystem.catalog.dto.response.WishlistResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

public interface WishlistService {
    String creatWishlist(WishlistRequest dto);

    Page<WishlistResponse> getAllWishlists(Pageable pageable);

    String deleteWishlist(Long productId);

    List<WishlistResponse> searchWishlist(String text);
}
