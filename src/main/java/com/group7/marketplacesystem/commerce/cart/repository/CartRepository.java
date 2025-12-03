package com.group7.marketplacesystem.commerce.cart.repository;

import com.group7.marketplacesystem.commerce.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByBuyerId(Integer buyerId);
}













