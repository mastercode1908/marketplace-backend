package com.group7.marketplacesystem.promotion.repository;

import com.group7.marketplacesystem.promotion.entity.Flashsale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashSaleRepository extends JpaRepository<Flashsale, Integer> {
}
