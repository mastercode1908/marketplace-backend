package com.group7.marketplacesystem.commerce.order.repository;

import com.group7.marketplacesystem.commerce.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByBuyerId(Integer buyerId);
    List<Order> findBySellerId(Integer sellerId);
    
    // Pagination methods
    Page<Order> findByBuyerIdOrderByOrderDateDesc(Integer buyerId, Pageable pageable);
    Page<Order> findBySellerIdOrderByOrderDateDesc(Integer sellerId, Pageable pageable);

    // Dashboard queries
    @Query(value = "SELECT COUNT(*) FROM `order` o WHERE DATE(o.order_date) = CURDATE()", nativeQuery = true)
    Long countOrdersToday();

    @Query(value = "SELECT COUNT(*) FROM `order` o WHERE o.order_status = :status", nativeQuery = true)
    Long countByOrderStatus(@Param("status") String status);

    @Query(value = "SELECT COALESCE(SUM(o.final_amount), 0) FROM `order` o WHERE YEAR(o.order_date) = YEAR(CURDATE()) AND MONTH(o.order_date) = MONTH(CURDATE()) AND o.order_status = 'Delivered'", nativeQuery = true)
    BigDecimal getRevenueThisMonth();

    @Query(value = "SELECT COALESCE(SUM(o.final_amount), 0) FROM `order` o WHERE YEAR(o.order_date) = YEAR(CURDATE()) AND o.order_status = 'Delivered'", nativeQuery = true)
    BigDecimal getRevenueThisYear();

    @Query(value = "SELECT COALESCE(SUM(o.final_amount), 0)\n" +
            "FROM `order` o\n" +
            "WHERE YEAR(o.order_date) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))\n" +
            "  AND MONTH(o.order_date) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) AND o.order_status = 'Delivered';", nativeQuery = true)
    BigDecimal getRevenueLastMonth();

    @Query(value = "SELECT COALESCE(SUM(o.final_amount), 0) FROM `order` o WHERE YEAR(o.order_date) = YEAR(CURDATE()) - 1", nativeQuery = true)
    BigDecimal getRevenueLastYear();

    @Query(value = "SELECT * FROM `order` o WHERE o.order_date >= :startDate AND o.order_date < :endDate", nativeQuery = true)
    List<Order> findByOrderDateBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query(value = "SELECT o.order_status, COUNT(*) FROM `order` o GROUP BY o.order_status", nativeQuery = true)
    List<Object[]> countByOrderStatusGrouped();

    // Revenue Report Queries
    @Query(value = "SELECT DATE(o.order_date) as period, " +
            "COALESCE(SUM(o.final_amount), 0) as revenue, " +
            "COUNT(*) as orderCount " +
            "FROM `order` o " +
            "WHERE o.order_date >= :startDate AND o.order_date < :endDate " +
            "AND o.order_status = 'Delivered' " +
            "AND (:sellerId IS NULL OR o.seller_id = :sellerId) " +
            "GROUP BY DATE(o.order_date) " +
            "ORDER BY period", nativeQuery = true)
    List<Object[]> getRevenueByDay(@Param("startDate") Instant startDate, 
                                    @Param("endDate") Instant endDate,
                                    @Param("sellerId") Integer sellerId);

    @Query(value = "SELECT DATE_FORMAT(o.order_date, '%Y-%m') as period, " +
            "COALESCE(SUM(o.final_amount), 0) as revenue, " +
            "COUNT(*) as orderCount " +
            "FROM `order` o " +
            "WHERE o.order_date >= :startDate AND o.order_date < :endDate " +
            "AND o.order_status = 'Delivered' " +
            "AND (:sellerId IS NULL OR o.seller_id = :sellerId) " +
            "GROUP BY DATE_FORMAT(o.order_date, '%Y-%m') " +
            "ORDER BY period", nativeQuery = true)
    List<Object[]> getRevenueByMonth(@Param("startDate") Instant startDate, 
                                     @Param("endDate") Instant endDate,
                                     @Param("sellerId") Integer sellerId);

    @Query(value =
            "SELECT CONCAT(CAST(YEAR(o.order_date) AS CHAR), '-Q', CAST(QUARTER(o.order_date) AS CHAR)) AS period, " +
                    "COALESCE(SUM(o.final_amount), 0) AS revenue, " +
                    "COUNT(*) AS orderCount " +
                    "FROM `order` o " +
                    "WHERE o.order_date >= :startDate " +
                    "  AND o.order_date < :endDate " +
                    "  AND o.order_status = 'Delivered' " +
                    "  AND (:sellerId IS NULL OR o.seller_id = :sellerId) " +
                    "GROUP BY period " +
                    "ORDER BY period",
            nativeQuery = true)
    List<Object[]> getRevenueByQuarter(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("sellerId") Integer sellerId
    );

    @Query(value = "SELECT YEAR(o.order_date) AS period, " +
            "COALESCE(SUM(o.final_amount), 0) AS revenue, " +
            "COUNT(*) AS orderCount " +
            "FROM `order` o " +
            "WHERE o.order_date >= :startDate " +
            "AND o.order_date < :endDate " +
            "AND o.order_status = 'Delivered' " +
            "AND (:sellerId IS NULL OR o.seller_id = :sellerId) " +
            "GROUP BY YEAR(o.order_date) " +
            "ORDER BY YEAR(o.order_date)",
            nativeQuery = true)
    List<Object[]> getRevenueByYear(@Param("startDate") Instant startDate,
                                    @Param("endDate") Instant endDate,
                                    @Param("sellerId") Integer sellerId);


    @Query(value = "SELECT DATE(o.order_date) as date, " +
            "COALESCE(SUM(o.final_amount), 0) as revenue, " +
            "COUNT(*) as orderCount " +
            "FROM `order` o " +
            "WHERE o.order_date >= :startDate AND o.order_date < :endDate " +
            "AND o.order_status = 'Delivered' " +
            "AND (:sellerId IS NULL OR o.seller_id = :sellerId) " +
            "GROUP BY DATE(o.order_date) " +
            "ORDER BY revenue DESC " +
            "LIMIT 10", nativeQuery = true)
    List<Object[]> getTopRevenueDays(@Param("startDate") Instant startDate, 
                                      @Param("endDate") Instant endDate,
                                      @Param("sellerId") Integer sellerId);

    @Query(value = "SELECT o.seller_id, " +
            "COALESCE(SUM(o.final_amount), 0) as revenue, " +
            "COUNT(*) as orderCount " +
            "FROM `order` o " +
            "WHERE o.order_date >= :startDate AND o.order_date < :endDate " +
            "AND o.order_status = 'Delivered' " +
            "GROUP BY o.seller_id " +
            "ORDER BY revenue DESC", nativeQuery = true)
    List<Object[]> getRevenueBySeller(@Param("startDate") Instant startDate, 
                                      @Param("endDate") Instant endDate);

    @Query(value = "SELECT COALESCE(SUM(o.final_amount), 0) " +
            "FROM `order` o " +
            "WHERE o.order_date >= :startDate AND o.order_date < :endDate " +
            "AND o.order_status = 'Delivered' " +
            "AND (:sellerId IS NULL OR o.seller_id = :sellerId)", nativeQuery = true)
    BigDecimal getTotalRevenue(@Param("startDate") Instant startDate, 
                               @Param("endDate") Instant endDate,
                               @Param("sellerId") Integer sellerId);

    @Query(value = "SELECT COUNT(*) " +
            "FROM `order` o " +
            "WHERE o.order_date >= :startDate AND o.order_date < :endDate " +
            "AND o.order_status = 'Delivered' " +
            "AND (:sellerId IS NULL OR o.seller_id = :sellerId)", nativeQuery = true)
    Long getTotalOrders(@Param("startDate") Instant startDate, 
                       @Param("endDate") Instant endDate,
                       @Param("sellerId") Integer sellerId);

    @Query(value = "SELECT COUNT(*) FROM `order` o WHERE o.seller_id = :sellerId", nativeQuery = true)
    long countBySellerId(@Param("sellerId") Integer sellerId);

    @Query(value = "SELECT COALESCE(SUM(o.final_amount),0) FROM `order` o WHERE o.seller_id = :sellerId AND o.order_status = 'Delivered'", nativeQuery = true)
    double sumRevenueBySellerId(@Param("sellerId") Integer sellerId);


    @Query(value = "SELECT COUNT(*) FROM `order` o WHERE o.seller_id = :sellerId AND o.order_status = :status", nativeQuery = true)
    Long countBySellerIdAndStatus(@Param("sellerId") Integer sellerId, @Param("status") String status);

    @Query(value = """
        SELECT DATE_FORMAT(o.order_date, '%Y-%m') AS month,
               COUNT(*) AS order_count,
               COALESCE(SUM(o.final_amount), 0) AS revenue
        FROM `order` o
        WHERE o.seller_id = :sellerId
        GROUP BY DATE_FORMAT(o.order_date, '%Y-%m')
        ORDER BY month
        """, nativeQuery = true)
    List<Object[]> getMonthlyOrderGrowth(@Param("sellerId") Integer sellerId);

    @Query(value = """
        SELECT DATE(o.order_date) AS day,
               COUNT(*) AS order_count,
               COALESCE(SUM(o.final_amount), 0) AS revenue
        FROM `order` o
        WHERE o.seller_id = :sellerId
        GROUP BY DATE(o.order_date)
        ORDER BY day
        """, nativeQuery = true)
    List<Object[]> getDailyOrderGrowth(@Param("sellerId") Integer sellerId);

    @Query(value = """
        SELECT DATE_FORMAT(o.order_date, '%Y-%u') AS week,
               COUNT(*) AS order_count,
               COALESCE(SUM(o.final_amount), 0) AS revenue
        FROM `order` o
        WHERE o.seller_id = :sellerId
        GROUP BY DATE_FORMAT(o.order_date, '%Y-%u')
        ORDER BY week
        """, nativeQuery = true)
    List<Object[]> getWeeklyOrderGrowth(@Param("sellerId") Integer sellerId);

}
