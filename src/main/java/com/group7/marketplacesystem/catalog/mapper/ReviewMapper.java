package com.group7.marketplacesystem.catalog.mapper;

import com.group7.marketplacesystem.catalog.dto.request.ReviewRequest;
import com.group7.marketplacesystem.catalog.dto.response.ReviewResponse;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Review;
import com.group7.marketplacesystem.identity.entity.Buyer;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public static Review toEntity(ReviewRequest dto, Product product, Buyer buyer) {
        if (dto == null || product == null || buyer == null) {
            return null;
        }
        Review review = new Review();
        review.setProduct(product);
        review.setBuyer(buyer);
        review.setRating(dto.getRating());
        review.setIsVerifiedPurchase(dto.getIsVerifiedPurchase() != null ? dto.getIsVerifiedPurchase() : false);
        review.setComment(dto.getComment());
        return review;
    }

    // Map từ Review entity → ReviewResponse để trả về client
    public ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        ReviewResponse response = ReviewResponse.builder()
                .reviewId(review.getId())
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .buyerId(review.getBuyer() != null ? review.getBuyer().getId() : null)
                .rating(review.getRating())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .deletedAt(review.getDeletedAt())
                .avatarUrl(review.getBuyer() != null && review.getBuyer().getUsers() != null ? review.getBuyer().getUsers().getAvatarUrl() : null)
                .buyerName(review.getBuyer() != null && review.getBuyer().getUsers() != null ? review.getBuyer().getUsers().getFullName() : "Người dùng")
                .build();
        return response;
    }
}
