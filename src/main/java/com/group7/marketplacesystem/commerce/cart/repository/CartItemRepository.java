package com.group7.marketplacesystem.commerce.cart.repository;

import com.group7.marketplacesystem.commerce.cart.entity.Cartitem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<Cartitem, Integer> {
    List<Cartitem> findByCartId(Integer cartId);

    Optional<Cartitem> findByCartIdAndProductId(Integer cartId, Integer productId);

    void deleteByCartId(Integer cartId);
}




