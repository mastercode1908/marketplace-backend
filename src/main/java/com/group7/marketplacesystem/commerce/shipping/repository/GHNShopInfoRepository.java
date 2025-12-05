package com.group7.marketplacesystem.commerce.shipping.repository;

import com.group7.marketplacesystem.commerce.shipping.entity.GHNShopInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GHNShopInfoRepository extends JpaRepository<GHNShopInfo, Integer> {
    Optional<GHNShopInfo> findBySellerId(Integer sellerId);
    boolean existsBySellerId(Integer sellerId);
}













