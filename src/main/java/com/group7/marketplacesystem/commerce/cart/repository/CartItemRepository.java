package com.group7.marketplacesystem.commerce.cart.repository;

import com.group7.marketplacesystem.commerce.cart.entity.Cartitem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<Cartitem, Integer> {
    @Query(value = "SELECT ci.*\n" +
            "FROM cartitem ci\n" +
            "JOIN product p ON p.product_id = ci.product_id\n" +
            "WHERE ci.cart_id = :cartId AND p.deleted_at IS NULL", nativeQuery = true)
    List<Cartitem> findByCartId(Integer cartId);

    Optional<Cartitem> findByCartIdAndProductId(Integer cartId, Integer productId);

    void deleteByCartId(Integer cartId);
}




