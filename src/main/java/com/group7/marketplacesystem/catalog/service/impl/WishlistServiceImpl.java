package com.group7.marketplacesystem.catalog.service.impl;

import com.group7.marketplacesystem.catalog.dto.request.WishlistRequest;
import com.group7.marketplacesystem.catalog.dto.response.WishlistResponse;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Wishlist;
import com.group7.marketplacesystem.catalog.entity.WishlistId;
import com.group7.marketplacesystem.catalog.mapper.WishlistMapper;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.catalog.repository.WishlistRepository;
import com.group7.marketplacesystem.catalog.service.WishlistService;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistMapper wishlistMapper;
    private final ProductRepository productRepository;
    private final BuyerRepository buyerRepository;
    private final UserRepository userRepository;

    @Override
    public String creatWishlist(WishlistRequest dto) {
        Product product = productRepository.findById(dto.getProductId().intValue())
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));
        Buyer buyer = buyerRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        Wishlist wishlist = new Wishlist();
        wishlist.setBuyer(buyer);
        wishlist.setProduct(product);

        WishlistId wishlistId = new WishlistId();
        wishlistId.setBuyerId(user.getId());
        wishlistId.setProductId(dto.getProductId());
        wishlist.setId(wishlistId);

        if (wishlistRepository.existsById(wishlistId)) {
            throw new RuntimeException("Sản phẩm đã có trong wishlist!");
        }

        wishlist.setAddedAt(Instant.now());

        wishlistRepository.save(wishlist);

        return "Bạn đã thêm sản phẩm này vào wishlist";
    }

    @Override
    public Page<WishlistResponse> getAllWishlists(Pageable pageable) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName() ;

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));
        Buyer buyer = buyerRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        Page<Wishlist> wishlists = wishlistRepository.findAllByBuyer(buyer, pageable);

        return wishlists.map(wishlistMapper::toResponse);
    }

    @Override
    public String deleteWishlist(Long productId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName() ;

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));
        Buyer buyer = buyerRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        WishlistId wishlistId = new WishlistId();
        wishlistId.setBuyerId(buyer.getId());
        wishlistId.setProductId(Integer.parseInt(productId.toString()));

        if (!wishlistRepository.existsById(wishlistId)) {
            throw new RuntimeException("Sản phẩm này không có trong wishlist!");
        }

        wishlistRepository.deleteById(wishlistId);

        return "Bạn đã xóa sản phẩm này ra khỏi wishlist";
    }

    @Override
    public List<WishlistResponse> searchWishlist(String text) {
        List<Wishlist> wishlists = wishlistRepository.searchByProductNameContaining(text);
        return wishlists.stream()
                .map(wishlistMapper::toResponse)
                .collect(Collectors.toList());
    }

}
