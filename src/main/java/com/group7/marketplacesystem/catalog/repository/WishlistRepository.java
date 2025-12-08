package com.group7.marketplacesystem.catalog.repository;

import com.group7.marketplacesystem.catalog.entity.Wishlist;
import com.group7.marketplacesystem.catalog.entity.WishlistId;
import com.group7.marketplacesystem.identity.entity.Buyer;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {
    boolean existsById(WishlistId id);
    Page<Wishlist> findAllByBuyer(Buyer buyer, Pageable pageable);
    List<Wishlist> findAllByBuyer(Buyer buyer);
    @Query("SELECT w FROM Wishlist w JOIN w.product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<Wishlist> searchByProductNameContaining(@Param("text") String text);
}
