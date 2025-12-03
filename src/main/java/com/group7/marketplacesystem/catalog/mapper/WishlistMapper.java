package com.group7.marketplacesystem.catalog.mapper;

import com.group7.marketplacesystem.catalog.dto.response.ProductMediaResponse;
import com.group7.marketplacesystem.catalog.dto.response.WishlistResponse;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Productmedia;
import com.group7.marketplacesystem.catalog.entity.Wishlist;
import com.group7.marketplacesystem.catalog.repository.ProductmediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.group7.marketplacesystem.catalog.mapper.ProductMediaMapper.toMediaResponseList;

@RequiredArgsConstructor
@Component
public class WishlistMapper {

    private final ProductmediaRepository productMediaRepository;

    // Map từ Wishlist entity → WishlistResponse
    public WishlistResponse toResponse(Wishlist wishlist) {
        if (wishlist == null) return null;

        Product product = wishlist.getProduct();
        List<Productmedia> media = productMediaRepository.findByProductId(product.getId());

        List<ProductMediaResponse> mediaResponses = toMediaResponseList(media);

        return WishlistResponse.builder()
                .BuyerId(wishlist.getBuyer() != null ? wishlist.getBuyer().getId() : null)
                .productId(product != null ? product.getId() : null)
                .addedAt(wishlist.getAddedAt())
                .productName(product != null ? product.getName() : null)
                .stockQuantity(product != null ? product.getStockQuantity() : null)
                .price(product != null ? product.getPrice() : null)
                .sellerId(product != null && product.getSeller() != null ? product.getSeller().getId() : null)
                .shopName(product != null && product.getSeller() != null ? product.getSeller().getShopName() : null)
                .media(mediaResponses)
                .build();
    }



    // Map từ Product + WishlistRequest → Wishlist entity
    public Wishlist toEntity(Product product) {
        if (product == null) {
            return null;
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setProduct(product);
        wishlist.setAddedAt(Instant.now());
        return wishlist;
    }
}
