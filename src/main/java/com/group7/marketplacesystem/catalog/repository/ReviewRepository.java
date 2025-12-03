package com.group7.marketplacesystem.catalog.repository;

import com.group7.marketplacesystem.catalog.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct_Id(Long productId);

    @Query(value = """
    SELECT COALESCE(AVG(r.rating), 0)
    FROM review r
    JOIN product p ON r.product_id = p.product_id
    WHERE p.seller_id = :sellerId
    """, nativeQuery = true)
    Double getSellerAverageRating(@Param("sellerId") Integer sellerId);

    @Query(value = " SELECT COUNT(*) FROM review r " +
            "JOIN product p ON r.product_id = p.product_id " +
            "WHERE p.seller_id = :sellerId", nativeQuery = true)
    Integer countBySellerId(@Param("sellerId") Integer sellerId);

    @Query(value = "SELECT COUNT(*) FROM review r " +
            "JOIN product p ON r.product_id = p.product_id " +
            "WHERE p.seller_id = :sellerId AND r.rating = :rating", nativeQuery = true)
    long countBySellerIdAndRating(@Param("sellerId") Integer sellerId, @Param("rating") int rating);
}
