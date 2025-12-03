package com.group7.marketplacesystem.commerce.order.repository;

import com.group7.marketplacesystem.commerce.order.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    Optional<Delivery> findByOrderId(Integer orderId);
    List<Delivery> findByOrderBuyerId(Integer buyerId);
    List<Delivery> findByOrderSellerId(Integer sellerId);
}

