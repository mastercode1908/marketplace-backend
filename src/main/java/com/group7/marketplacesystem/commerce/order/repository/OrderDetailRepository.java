package com.group7.marketplacesystem.commerce.order.repository;

import com.group7.marketplacesystem.commerce.order.entity.Orderdetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDetailRepository extends JpaRepository<Orderdetail, Integer> {
    List<Orderdetail> findByOrderId(Integer orderId);

    Optional<Orderdetail> findById(Integer orderDetailId);


    // Query to get top products by seller
    @Query(value = "SELECT od.product.id as productId, " +
           "od.product.productName as productName, " +
           "od.product.imageUrl as imageUrl, " +
           "SUM(od.quantity) as soldQuantity, " +
           "SUM(od.subtotal) as totalRevenue " +
           "FROM Orderdetail od " +
           "WHERE od.order.seller.id = :sellerId " +
           "AND od.order.orderStatus = 'Delivered' " +
           "GROUP BY od.product.id, od.product.productName, od.product.imageUrl " +
           "ORDER BY SUM(od.subtotal) DESC", nativeQuery = true)
    List<Object[]> getTopProductsBySeller(@Param("sellerId") Integer sellerId);

    // Query to get total sold quantity for a product
    @Query("SELECT COALESCE(SUM(od.quantity), 0) " +
           "FROM Orderdetail od " +
           "WHERE od.product.id = :productId " +
           "AND od.order.orderStatus IN ('Delivered')")
    Long getSoldQuantityByProductId(@Param("productId") Integer productId);

}













