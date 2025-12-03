package com.group7.marketplacesystem.promotion.repository;

import com.group7.marketplacesystem.promotion.entity.Promotionusage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionUsageRepository extends JpaRepository<Promotionusage, Long> {
}