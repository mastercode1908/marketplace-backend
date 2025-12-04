package com.group7.marketplacesystem.identity.repository;

import com.group7.marketplacesystem.identity.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findAllByRole(String role);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.userStatus = :status WHERE u.id = :entityId")
    void updateStatus(@Param("entityId") Integer entityId, @Param("status") String status);

    @Query(value = """
        SELECT u.*
        FROM users u
        WHERE
            (:username IS NULL OR u.username LIKE CONCAT('%', :username, '%'))
            AND (:email IS NULL OR u.email LIKE CONCAT('%', :email, '%'))
            AND (:phone IS NULL OR u.phone LIKE CONCAT('%', :phone, '%'))
            AND (:role IS NULL OR u.role = :role)
            AND u.role NOT IN ('SYSTEMADMIN', 'CONTENTADMIN')
            AND (:user_status IS NULL OR u.user_status = :user_status)
            AND u.deleted_at IS NULL
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM users u
        WHERE
            (:username IS NULL OR u.username LIKE CONCAT('%', :username, '%'))
            AND (:email IS NULL OR u.email LIKE CONCAT('%', :email, '%'))
            AND (:phone IS NULL OR u.phone LIKE CONCAT('%', :phone, '%'))
            AND (:role IS NULL OR u.role = :role)
            AND (:user_status IS NULL OR u.user_status = :user_status)
            AND u.deleted_at IS NULL
        """,
            nativeQuery = true)
    Page<User> searchUsers(
            @Param("username") String username,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("role") String role,
            @Param("user_status") String userStatus,
            Pageable pageable
    );


    @Query(value = "SELECT u.*\n" +
            "FROM users u\n" +
            "WHERE \n" +
            "     (u.deleted_at IS NULL)\n" +
            "    AND u.user_id = :id", nativeQuery = true)
    Optional<User> findByIdByDeletedAtIsNull(@Param("id") Integer id);
    @Query(value = "SELECT u.* FROM users u WHERE  (u.role = 'SELLER' or u.role = 'BUYER') AND u.user_status = 'Active'", nativeQuery = true)
    List<User>findAllActiveUsers();

    @Query(value = "SELECT u.* FROM users u WHERE  u.role = 'BUYER' AND u.user_status = 'Active'", nativeQuery = true)
    List<User> findAllBuyer();

    @Query(value = "SELECT u.* FROM users u WHERE  u.role = 'SELLER' AND u.user_status = 'Active'", nativeQuery = true)
    List<User>findBySeller();

    Page<User> findByRoleAndUserStatus(String role, String userStatus, Pageable pageable);

    // Dashboard queries
    @Query(value = "SELECT COUNT(*) FROM users u WHERE DATE(u.created_at) = CURRENT_DATE AND u.deleted_at IS NULL", nativeQuery = true)
    Long countNewUsersToday();

    @Query(value = "SELECT COUNT(*) FROM users u WHERE DATE(u.created_at) = CURRENT_DATE AND u.role = 'BUYER' AND u.deleted_at IS NULL", nativeQuery = true)
    Long countNewBuyersToday();

    @Query(value = "SELECT COUNT(*) FROM users u WHERE DATE(u.created_at) = CURRENT_DATE AND u.role = 'SELLER' AND u.deleted_at IS NULL", nativeQuery = true)
    Long countNewSellersToday();

    @Query(value = "SELECT COUNT(*) FROM users u WHERE u.user_status = 'Active' AND u.deleted_at IS NULL", nativeQuery = true)
    Long countActiveUsers();

    @Query(value = "SELECT COUNT(*) FROM users u WHERE u.user_status IN ('Banned') AND u.deleted_at IS NULL", nativeQuery = true)
    Long countBannedUsers();

    @Query(value = "SELECT COUNT(*) FROM users u WHERE u.role = 'SELLER' AND YEAR(u.created_at) = YEAR(CURRENT_DATE) AND MONTH(u.created_at) = MONTH(CURRENT_DATE) AND u.deleted_at IS NULL", nativeQuery = true)
    Long countNewSellersThisMonth();

    @Query(value = "SELECT COUNT(*) FROM users u WHERE u.role = 'SELLER' AND u.user_status = 'Active' AND u.deleted_at IS NULL", nativeQuery = true)
    Long countActiveSellers();

    @Query(value = "SELECT COUNT(*) FROM users u WHERE u.role = 'SELLER' AND u.user_status IN ('Banned') AND u.deleted_at IS NULL", nativeQuery = true)
    Long countBannedSellers();

    @Query(value = "SELECT DATE_FORMAT(u.created_at, '%Y-%m') as month, COUNT(*) as count " +
            "FROM users u WHERE u.role = :role AND u.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 12 MONTH) " +
            "AND u.deleted_at IS NULL GROUP BY DATE_FORMAT(u.created_at, '%Y-%m') ORDER BY month", nativeQuery = true)
    List<Object[]> countUsersByMonth(@Param("role") String role);

    @Query(value = "SELECT u.* FROM users u WHERE " +
            "(u.username LIKE CONCAT('%', :searchTerm, '%') OR u.email LIKE CONCAT('%', :searchTerm, '%')) " +
            "AND u.user_status = 'Active' AND u.deleted_at IS NULL " +
            "ORDER BY u.username ASC LIMIT 20", nativeQuery = true)
    List<User> findByUsernameContainingOrEmailContaining(@Param("searchTerm") String searchTerm);
}
