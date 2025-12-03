package com.group7.marketplacesystem.catalog.service.impl;

import com.group7.marketplacesystem.catalog.dto.request.ReviewRequest;
import com.group7.marketplacesystem.catalog.dto.response.ReviewResponse;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Review;
import com.group7.marketplacesystem.catalog.mapper.ReviewMapper;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.catalog.repository.ReviewRepository;
import com.group7.marketplacesystem.catalog.service.ReviewService;
import com.group7.marketplacesystem.commerce.order.entity.Orderdetail;
import com.group7.marketplacesystem.commerce.order.repository.OrderDetailRepository;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final ProductRepository productRepository;
    private final BuyerRepository buyerRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public ReviewResponse creatReview(ReviewRequest dto) {

        Product product = productRepository.findById(dto.getProductId().intValue())
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName() ;

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));
        Buyer buyer = buyerRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        Review review = new Review();
        review.setComment(dto.getComment());
        review.setRating(dto.getRating());
        review.setProduct(product);
        review.setBuyer(buyer);
        review.setCreatedAt(Instant.now());
        review.setIsVerifiedPurchase(dto.getIsVerifiedPurchase());

        reviewRepository.save(review);

        Orderdetail orderdetail = orderDetailRepository.findById(dto.getOrderDetailId())
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_DETAIL_NOT_FOUND));
        orderdetail.setIsReviewed(true);
        orderDetailRepository.save(orderdetail);

        return reviewMapper.toResponse(review);
    }



    @Override
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));
        return  reviewMapper.toResponse(review);
    }

    @Override
    public List<ReviewResponse> getAllReviewByProductId(Long productId) {
        return reviewRepository.findByProduct_Id(productId)
                .stream()
                .filter(review -> review.getDeletedAt() == null)
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    public ReviewResponse updateReview(Long id, ReviewRequest dto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName() ;

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));
        Buyer buyer = buyerRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));

        if(dto.getRating() != null) {
            review.setRating(dto.getRating());
        }

        if(dto.getComment() != null) {
            review.setComment(dto.getComment());
        }

        review.setUpdatedAt(Instant.now());

        reviewRepository.save(review);

        return reviewMapper.toResponse(review);

    }

    @Override
    public ReviewResponse deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));

        review.setDeletedAt(Instant.now());

        reviewRepository.save(review);

        return reviewMapper.toResponse(review);
    }

}
