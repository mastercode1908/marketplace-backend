package com.group7.marketplacesystem.commerce.payment.repository;

import com.group7.marketplacesystem.commerce.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByTargetIdAndTargetType(Integer targetId, String targetType);
    List<Payment> findByTargetType(String targetType);

    // Revenue Report Queries for Payment Methods
    @Query(value = "SELECT COALESCE(SUM(p.amount), 0) " +
            "FROM payment p " +
            "INNER JOIN `order` o ON p.target_id = o.order_id AND p.target_type = 'Order' " +
            "WHERE o.order_date >= :startDate AND o.order_date < :endDate " +
            "AND p.status = 'Success' " +
            "AND p.method = 'COD' " +
            "AND o.order_status = 'Delivered' " +
            "AND (:sellerId IS NULL OR o.seller_id = :sellerId)", nativeQuery = true)
    BigDecimal getTotalCodAmount(@Param("startDate") Instant startDate, 
                                  @Param("endDate") Instant endDate,
                                  @Param("sellerId") Integer sellerId);

    @Query(value = "SELECT COALESCE(SUM(p.amount), 0) " +
            "FROM payment p " +
            "INNER JOIN `order` o ON p.target_id = o.order_id AND p.target_type = 'Order' " +
            "WHERE o.order_date >= :startDate AND o.order_date < :endDate " +
            "AND p.status = 'Success' " +
            "AND p.method != 'VNPAY' " +
            "AND o.order_status = 'Delivered' " +
            "AND (:sellerId IS NULL OR o.seller_id = :sellerId)", nativeQuery = true)
    BigDecimal getTotalOnlineAmount(@Param("startDate") Instant startDate, 
                                     @Param("endDate") Instant endDate,
                                     @Param("sellerId") Integer sellerId);

    @Query(value = "SELECT o.seller_id, " +
            "COALESCE(SUM(CASE WHEN p.method = 'COD' THEN p.amount ELSE 0 END), 0) as codAmount, " +
            "COALESCE(SUM(CASE WHEN p.method != 'VNPAY' THEN p.amount ELSE 0 END), 0) as onlineAmount " +
            "FROM payment p " +
            "INNER JOIN `order` o ON p.target_id = o.order_id AND p.target_type = 'Order' " +
            "WHERE o.order_date >= :startDate AND o.order_date < :endDate " +
            "AND p.status = 'Success' " +
            "AND o.order_status = 'Delivered' " +
            "GROUP BY o.seller_id", nativeQuery = true)
    List<Object[]> getPaymentAmountsBySeller(@Param("startDate") Instant startDate, 
                                              @Param("endDate") Instant endDate);

    // Service Package Revenue Queries
    @Query(value = "SELECT COALESCE(SUM(p.amount), 0) " +
            "FROM payment p " +
            "INNER JOIN payment_session ps ON p.target_id = ps.target_id AND p.target_type = 'Package' AND ps.target_type = 'Package' " +
            "WHERE p.created_at >= :startDate AND p.created_at < :endDate " +
            "AND p.status = 'Paid' " +
            "AND p.target_type = 'Package' " +
            "AND (:sellerId IS NULL OR ps.seller_id = :sellerId)", nativeQuery = true)
    BigDecimal getTotalServicePackageRevenue(@Param("startDate") Instant startDate,
                                              @Param("endDate") Instant endDate,
                                              @Param("sellerId") Integer sellerId);

    @Query(value = "SELECT COUNT(DISTINCT p.payment_id) " +
            "FROM payment p " +
            "INNER JOIN payment_session ps ON p.target_id = ps.target_id AND p.target_type = 'Package' AND ps.target_type = 'Package' " +
            "WHERE p.created_at >= :startDate AND p.created_at < :endDate " +
            "AND p.status = 'Paid' " +
            "AND p.target_type = 'Package' " +
            "AND (:sellerId IS NULL OR ps.seller_id = :sellerId)", nativeQuery = true)
    Long getTotalServicePackageCount(@Param("startDate") Instant startDate,
                                     @Param("endDate") Instant endDate,
                                     @Param("sellerId") Integer sellerId);

    @Query(value = "SELECT DATE_FORMAT(p.created_at, :dateFormat) as period, " +
            "COALESCE(SUM(p.amount), 0) as revenue, " +
            "COUNT(DISTINCT p.payment_id) as packageCount " +
            "FROM payment p " +
            "INNER JOIN payment_session ps ON p.target_id = ps.target_id AND p.target_type = 'Package' AND ps.target_type = 'Package' " +
            "WHERE p.created_at >= :startDate AND p.created_at < :endDate " +
            "AND p.status = 'Paid' " +
            "AND p.target_type = 'Package' " +
            "AND (:sellerId IS NULL OR ps.seller_id = :sellerId) " +
            "GROUP BY period " +
            "ORDER BY period", nativeQuery = true)
    List<Object[]> getServicePackageRevenueByPeriod(@Param("startDate") Instant startDate,
                                                     @Param("endDate") Instant endDate,
                                                     @Param("sellerId") Integer sellerId,
                                                     @Param("dateFormat") String dateFormat);

    @Query(value = "SELECT ps.seller_id, " +
            "COALESCE(SUM(p.amount), 0) as revenue, " +
            "COUNT(DISTINCT p.payment_id) as packageCount " +
            "FROM payment p " +
            "INNER JOIN payment_session ps ON p.target_id = ps.target_id AND p.target_type = 'Package' AND ps.target_type = 'Package' " +
            "WHERE p.created_at >= :startDate AND p.created_at < :endDate " +
            "AND p.status = 'Paid' " +
            "AND p.target_type = 'Package' " +
            "GROUP BY ps.seller_id " +
            "ORDER BY revenue DESC", nativeQuery = true)
    List<Object[]> getServicePackageRevenueBySeller(@Param("startDate") Instant startDate,
                                                     @Param("endDate") Instant endDate);
}









