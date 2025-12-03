package com.group7.marketplacesystem.catalog;

import com.group7.marketplacesystem.catalog.dto.request.ReviewRequest;
import com.group7.marketplacesystem.catalog.dto.response.ReviewResponse;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Review;
import com.group7.marketplacesystem.catalog.mapper.ReviewMapper;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.catalog.repository.ReviewRepository;
import com.group7.marketplacesystem.catalog.service.impl.ReviewServiceImpl;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Review Service Tests")
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BuyerRepository buyerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private Product product;
    private Buyer buyer;
    private User user;
    private ReviewRequest reviewRequest;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        // Setup user
        user = new User();
        user.setId(1);
        user.setEmail("buyer@example.com");

        // Setup buyer
        buyer = new Buyer();
        buyer.setId(1);
        buyer.setUsers(user);

        // Setup product
        product = new Product();
        product.setId(1);
        product.setName("iPhone 15 Pro");
        product.setPrice(new BigDecimal("29999000"));

        // Setup review
        review = new Review();
        review.setId(1);
        review.setProduct(product);
        review.setBuyer(buyer);
        review.setRating(5);
        review.setComment("Sản phẩm rất tốt, đáng tiền!");
        review.setIsVerifiedPurchase(true);
        review.setCreatedAt(Instant.now());

        // Setup request
        reviewRequest = new ReviewRequest();
        reviewRequest.setProductId(1);
        reviewRequest.setRating(5);
        reviewRequest.setComment("Sản phẩm rất tốt, đáng tiền!");
        reviewRequest.setIsVerifiedPurchase(true);

        // Setup response
        reviewResponse = new ReviewResponse();
        reviewResponse.setReviewId(1);
        reviewResponse.setProductId(1);
        reviewResponse.setRating(5);
        reviewResponse.setComment("Sản phẩm rất tốt, đáng tiền!");
    }

    @Test
    @DisplayName("Should create review successfully")
    void testCreateReview_Success() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");
        
        when(productRepository.findById(anyInt())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.of(buyer));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(reviewResponse);

        // When
        ReviewResponse result = reviewService.creatReview(reviewRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Sản phẩm rất tốt, đáng tiền!");
        
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(productRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findByEmail("buyer@example.com");
        
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testCreateReview_ProductNotFound() {
        when(productRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.creatReview(reviewRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("PRODUCT_NOT_FOUND");

        verify(reviewRepository, never()).save(any(Review.class));
    }


    @Test
    @DisplayName("Should throw exception when buyer not found")
    void testCreateReview_BuyerNotFound() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");
        
        when(productRepository.findById(anyInt())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.creatReview(reviewRequest))
            .isInstanceOf(ApiException.class);
        
        verify(reviewRepository, never()).save(any(Review.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should get review by id successfully")
    void testGetReviewById_Success() {
        // Given
        Long reviewId = 1L;
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(reviewResponse);

        // When
        ReviewResponse result = reviewService.getReviewById(reviewId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(1L);
        verify(reviewRepository, times(1)).findById(reviewId);
    }

    @Test
    @DisplayName("Should throw exception when review not found by id")
    void testGetReviewById_NotFound() {
        // Given
        Long reviewId = 999L;
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.getReviewById(reviewId))
            .isInstanceOf(ApiException.class);
        
        verify(reviewRepository, times(1)).findById(reviewId);
    }

    @Test
    @DisplayName("Should get all reviews by product id")
    void testGetAllReviewByProductId_Success() {
        // Given
        Long productId = 1L;
        Review review2 = new Review();
        review2.setId(2);
        review2.setRating(4);
        review2.setComment("Good product");
        
        List<Review> reviews = Arrays.asList(review, review2);
        
        when(reviewRepository.findByProduct_Id(anyLong())).thenReturn(reviews);
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(reviewResponse);

        // When
        List<ReviewResponse> result = reviewService.getAllReviewByProductId(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(reviewRepository, times(1)).findByProduct_Id(productId);
    }

    @Test
    @DisplayName("Should filter out deleted reviews")
    void testGetAllReviewByProductId_FilterDeleted() {
        // Given
        Long productId = 1L;
        
        Review deletedReview = new Review();
        deletedReview.setId(2);
        deletedReview.setRating(3);
        deletedReview.setDeletedAt(Instant.now());
        
        List<Review> reviews = Arrays.asList(review, deletedReview);
        
        when(reviewRepository.findByProduct_Id(anyLong())).thenReturn(reviews);
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(reviewResponse);

        // When
        List<ReviewResponse> result = reviewService.getAllReviewByProductId(productId);

        // Then
        assertThat(result).hasSize(1); // Only non-deleted review
        verify(reviewMapper, times(1)).toResponse(any(Review.class));
    }

    @Test
    @DisplayName("Should update review successfully")
    void testUpdateReview_Success() {
        // Given
        Long reviewId = 1L;
        ReviewRequest updateRequest = new ReviewRequest();
        updateRequest.setRating(4);
        updateRequest.setComment("Sản phẩm tốt nhưng hơi đắt");
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.of(buyer));
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(reviewResponse);

        // When
        ReviewResponse result = reviewService.updateReview(reviewId, updateRequest);

        // Then
        assertThat(review.getRating()).isEqualTo(4);
        assertThat(review.getComment()).isEqualTo("Sản phẩm tốt nhưng hơi đắt");
        assertThat(review.getUpdatedAt()).isNotNull();
        verify(reviewRepository, times(1)).save(review);
        
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should update only rating when comment is null")
    void testUpdateReview_OnlyRating() {
        // Given
        Long reviewId = 1L;
        ReviewRequest updateRequest = new ReviewRequest();
        updateRequest.setRating(3);
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.of(buyer));
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(reviewResponse);

        // When
        reviewService.updateReview(reviewId, updateRequest);

        // Then
        assertThat(review.getRating()).isEqualTo(3);
        verify(reviewRepository, times(1)).save(review);
        
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent review")
    void testUpdateReview_NotFound() {
        // Given
        Long reviewId = 999L;
        ReviewRequest updateRequest = new ReviewRequest();
        updateRequest.setRating(4);
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.of(buyer));
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.updateReview(reviewId, updateRequest))
            .isInstanceOf(ApiException.class);
        
        verify(reviewRepository, never()).save(any(Review.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should soft delete review successfully")
    void testDeleteReview_Success() {
        // Given
        Long reviewId = 1L;
        
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(reviewResponse);

        // When
        ReviewResponse result = reviewService.deleteReview(reviewId);

        // Then
        assertThat(review.getDeletedAt()).isNotNull();
        assertThat(result).isNotNull();
        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent review")
    void testDeleteReview_NotFound() {
        // Given
        Long reviewId = 999L;
        
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.deleteReview(reviewId))
            .isInstanceOf(ApiException.class);
        
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should create review with verified purchase flag")
    void testCreateReview_WithVerifiedPurchase() {
        // Given
        reviewRequest.setIsVerifiedPurchase(true);
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");
        
        when(productRepository.findById(anyInt())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.of(buyer));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            assertThat(savedReview.getIsVerifiedPurchase()).isTrue();
            return savedReview;
        });
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(reviewResponse);

        // When
        reviewService.creatReview(reviewRequest);

        // Then
        verify(reviewRepository, times(1)).save(any(Review.class));
        SecurityContextHolder.clearContext();
    }
}
