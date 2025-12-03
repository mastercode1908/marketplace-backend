package com.group7.marketplacesystem.catalog.repository;

import com.group7.marketplacesystem.catalog.entity.Product;
import org.springframework.data.domain.Pageable;
import com.group7.marketplacesystem.identity.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findBySellerId(Integer sellerId);

    List<Product> findAllByDeletedAtIsNullOrderByIdDesc();

    List<Product> findBySellerIdAndDeletedAtIsNullOrderByIdDesc(Integer sellerId);

    List<Product> findAllByDeletedAtIsNull();

    List<Product> findAllProductBySellerIn(List<Seller> sellers);

    List<Product> findAllById(Iterable<Integer> ids);

    List<Product> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name);

    // Dashboard queries
    @Query(value = "SELECT COUNT(*) FROM product p WHERE p.deleted_at IS NULL", nativeQuery = true)
    Long countTotalProducts();

    @Query(value = "SELECT COUNT(*) FROM product p WHERE p.deleted_at IS NULL AND p.product_status = :status", nativeQuery = true)
    Long countByProductStatus(@Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM product p WHERE p.deleted_at IS NULL AND p.stock_quantity = 0", nativeQuery = true)
    Long countOutOfStockProducts();

    @Query(value = "SELECT p.category_id, COUNT(*) FROM product p WHERE p.deleted_at IS NULL GROUP BY p.category_id", nativeQuery = true)
    List<Object[]> countByCategory();

    @Query(value = "SELECT COUNT(*) FROM product p WHERE p.deleted_at IS NULL AND p.seller_id = :sellerId", nativeQuery = true)
    long countBySellerId(@Param("sellerId") Integer sellerId);

}
