package com.group7.marketplacesystem.identity.repository;

import com.group7.marketplacesystem.identity.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Integer> {
    boolean existsByTaxCode(String tax);

    List<Seller> findByUsers_Id(Integer id);

    Optional<Seller> findById(Integer id);
//    List<Seller> findAllByIdIn(List<Integer> sellerIds);
}
