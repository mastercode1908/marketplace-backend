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
    @Query(value = "SELECT COALESCE(SUM(p.amount), 0)\n" +
            "            FROM paymentvnpay p\n" +
            "            INNER JOIN payment_session ps \n" +
            "                ON CONVERT(p.vnp_txn_ref USING utf8mb4) = CONVERT(ps.txn_ref USING utf8mb4)\n" +
            "            WHERE p.created_at >= :startDate\n" +
            "              AND p.created_at < :endDate\n" +
            "              AND p.status = 'Success'\n" +
            "              AND ps.status = 'COMPLETED'\n" +
            "              AND ps.target_type = 'Package'\n" +
            "              AND (:sellerId IS NULL OR ps.seller_id = :sellerId);", nativeQuery = true)
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

    @Query(value = "SELECT \n" +
            "    DATE_FORMAT(p.created_at, :dateFormat) AS period,\n" +
            "    COALESCE(SUM(p.amount), 0) AS revenue,\n" +
            "    COUNT(DISTINCT p.id) AS packageCount\n" +
            "FROM paymentvnpay p\n" +
            "INNER JOIN payment_session ps\n" +
            "    ON CONVERT(p.vnp_txn_ref USING utf8mb4) = CONVERT(ps.txn_ref USING utf8mb4)\n" +
            "WHERE p.created_at >= :startDate\n" +
            "  AND p.created_at < :endDate\n" +
            "  AND p.status = 'Success'\n" +
            "  AND ps.status = 'COMPLETED'\n" +
            "  AND ps.target_type = 'Package'\n" +
            "  AND (:sellerId IS NULL OR ps.seller_id = :sellerId)\n" +
            "GROUP BY period\n" +
            "ORDER BY period;", nativeQuery = true)
    List<Object[]> getServicePackageRevenueByPeriod(@Param("startDate") Instant startDate,
                                                    @Param("endDate") Instant endDate,
                                                    @Param("sellerId") Integer sellerId,
                                                    @Param("dateFormat") String dateFormat);

    @Query(value = "SELECT \n" +
            "    ps.seller_id,\n" +
            "    COALESCE(SUM(p.amount), 0) AS revenue,\n" +
            "    COUNT(DISTINCT p.id) AS packageCount\n" +
            "FROM paymentvnpay p\n" +
            "INNER JOIN payment_session ps \n" +
            "    ON p.vnp_txn_ref COLLATE utf8mb4_unicode_ci \n" +
            "       = ps.txn_ref COLLATE utf8mb4_unicode_ci\n" +
            "WHERE \n" +
            "    p.status COLLATE utf8mb4_unicode_ci = 'Success'\n" +
            "    AND ps.status COLLATE utf8mb4_unicode_ci = 'COMPLETED'\n" +
            "    AND ps.target_type COLLATE utf8mb4_unicode_ci = 'Package'\n" +
            "    AND p.created_at >= :startDate\n" +
            "    AND p.created_at < :endDate\n" +
            "GROUP BY \n" +
            "    ps.seller_id\n" +
            "ORDER BY \n" +
            "    revenue DESC;", nativeQuery = true)
    List<Object[]> getServicePackageRevenueBySeller(@Param("startDate") Instant startDate,
                                                     @Param("endDate") Instant endDate);
}









