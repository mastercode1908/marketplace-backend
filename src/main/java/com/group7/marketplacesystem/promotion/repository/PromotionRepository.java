package com.group7.marketplacesystem.promotion.repository;

import com.group7.marketplacesystem.promotion.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    // tìm tất cả các promotions có delete_at là null
    @Query(value = "SELECT * FROM promotion WHERE deleted_at IS NULL", nativeQuery = true)
    Page<Promotion> findAllActive(Pageable pageable);

    // tìm all promotion theo id seller
    @Query(value = "SELECT * FROM promotion WHERE deleted_at IS NULL AND owner_id = :userid", nativeQuery = true)
    Page<Promotion> findAllActiveByIdUser(Pageable pageable,@Param("userid") Integer userId);

    @Query(value = "SELECT * FROM promotion WHERE deleted_at IS NULL AND owner_id = :userid", nativeQuery = true)
    List<Promotion> findAllActiveByIdUser(@Param("userid") Integer userId);

    // tìm promotion theo id
    @Query(value = "SELECT * FROM promotion p WHERE p.promotion_id = :id AND p.deleted_at IS NULL", nativeQuery = true)
    Optional<Promotion> findByIdActive(@Param("id") Integer id);

    // khi theem 1 promotion cần kiểm tra mã promotion code của nó đã tồn tại hay chưa
    @Query(value = "SELECT EXISTS(SELECT 1 FROM promotion p WHERE p.promotion_code = :promotionCode AND p.deleted_at IS NULL)", nativeQuery = true)
    Long existsByPromotionCodeAndNotDeleted(@Param("promotionCode") String promotionCode);

    // Khi update promotion, nếu muốn sử mã code của promotion cần check lại mã đấy đã có bị trùng với các promotion khác hay không
    @Query(value = "SELECT EXISTS(SELECT 1 FROM promotion p WHERE p.promotion_code = :promotionCode AND p.promotion_id != :id AND p.deleted_at IS NULL)", nativeQuery = true)
    Long existsByPromotionCodeAndIdNotAndNotDeleted(@Param("promotionCode") String promotionCode, @Param("id") Integer id);

    // lấy ra những promption có end date < ngày hiện tại, để tự động cật nhật xóa promotion đó
    @Query(value = "SELECT * FROM promotion\n" +
            "WHERE end_date < :today\n" +
            "  AND deleted_at IS NULL",  nativeQuery = true)
    List<Promotion> findByEndDateBeforeAndDeletedAtIsNull(@Param("today") LocalDate today);



    Optional<Promotion> findByPromotionCode(String code);
}
