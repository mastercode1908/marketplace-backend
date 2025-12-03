package com.group7.marketplacesystem.catalog.service;

import com.group7.marketplacesystem.catalog.dto.request.ReviewRequest;
import com.group7.marketplacesystem.catalog.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse creatReview(ReviewRequest dto);

    ReviewResponse getReviewById(Long id);

    List<ReviewResponse> getAllReviewByProductId(Long productId);

    ReviewResponse updateReview(Long id, ReviewRequest dto);

    ReviewResponse deleteReview(Long id);

}
