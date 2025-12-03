package com.group7.marketplacesystem.identity.repository;

import com.group7.marketplacesystem.identity.entity.BuyerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerAddressRepository extends JpaRepository<BuyerAddress, Integer> {
    // Lấy tất cả địa chỉ của buyer (chưa bị xóa)
    List<BuyerAddress> findByBuyerIdAndDeletedAtIsNull(Integer buyerId);
    
    // Lấy địa chỉ mặc định của buyer (chưa bị xóa)
    Optional<BuyerAddress> findByBuyerIdAndIsDefaultTrueAndDeletedAtIsNull(Integer buyerId);
    
    // Lấy địa chỉ theo ID (bao gồm cả đã xóa - để kiểm tra ownership)
    // Method này dùng cho getAddressById và deleteAddress
    // @Query("SELECT a FROM BuyerAddress a WHERE a.id = :addressId")
    // Optional<BuyerAddress> findById(@Param("addressId") Integer addressId);
}


