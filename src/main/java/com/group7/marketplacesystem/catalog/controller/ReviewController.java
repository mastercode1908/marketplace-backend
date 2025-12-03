package com.group7.marketplacesystem.catalog.controller;

import com.group7.marketplacesystem.catalog.dto.request.ReviewRequest;
import com.group7.marketplacesystem.catalog.dto.response.ReviewResponse;
import com.group7.marketplacesystem.catalog.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'SELLER', 'BUYER')")
    @GetMapping("/products/{product_id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getAllReviewsByProductId(
            @PathVariable("product_id") long productId) {
        return ResponseEntity.ok(reviewService.getAllReviewByProductId(productId));
    }

    @PreAuthorize("hasRole('BUYER')")
    @PostMapping("/reviews/{product_id}")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable("product_id") Long productId,
            @RequestBody ReviewRequest reviewRequest) {

        reviewRequest.setProductId(Integer.parseInt(productId.toString()));

        ReviewResponse response = reviewService.creatReview(reviewRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('BUYER')")
    @PatchMapping("/reviews/{review_id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable("review_id") Integer reviewId,
            @RequestBody ReviewRequest reviewRequest) {

        ReviewResponse updatedReview = reviewService.updateReview(Long.valueOf(reviewId), reviewRequest);

        return ResponseEntity.ok(updatedReview);
    }


    @PreAuthorize("hasRole('BUYER')")
    @PostMapping("/reviews/{review_id}/delete")
    public ResponseEntity<ReviewResponse> deleteReview(
            @PathVariable("review_id") Integer reviewId,
            @RequestBody ReviewRequest reviewRequest){

        ReviewResponse existingReview = reviewService.getReviewById(Long.parseLong(reviewId.toString()));

        existingReview.setDeletedAt(Instant.now());

        ReviewResponse deletedReview = reviewService.deleteReview(Long.parseLong(reviewId.toString()));

        return ResponseEntity.ok(deletedReview);
    }
}
