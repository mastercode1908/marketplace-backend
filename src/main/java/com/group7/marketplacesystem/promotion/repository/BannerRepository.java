package com.group7.marketplacesystem.promotion.repository;

import com.group7.marketplacesystem.promotion.entity.Banner;
import com.group7.marketplacesystem.promotion.entity.Banner.BannerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Integer> {

    @Query("SELECT b FROM Banner b WHERE b.status = :status AND b.deletedAt IS NULL ORDER BY b.priority DESC, b.position DESC")
    List<Banner> findActiveBanners(@Param("status") BannerStatus status);


    // Lấy banner của seller, phân trang
    @Query("""
                SELECT b FROM Banner b
                WHERE b.seller.id = :sellerId
                  AND b.deletedAt IS NULL
                ORDER BY b.createdAt DESC
            """)
    Page<Banner> findBySeller_IdOrderByCreatedAtDesc(Integer sellerId, Pageable pageable);

    // Lấy banner theo status, phân trang
    Page<Banner> findByStatusOrderByCreatedAtDesc(Banner.BannerStatus status, Pageable pageable);


    @Query("SELECT b FROM Banner b WHERE b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    Page<Banner> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Banner> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT b FROM Banner b WHERE b.status = :status AND b.deletedAt IS NULL")
    List<Banner> findByStatus(@Param("status") Banner.BannerStatus status);

    @Modifying
    @Query("UPDATE Banner b SET b.status = 'COMPLETED' " +
            "WHERE b.status = 'ACTIVE' AND b.endDate <= :now")
    void completeExpiredBanners(@Param("now") Instant now);
}
