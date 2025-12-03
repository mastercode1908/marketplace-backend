package com.group7.marketplacesystem.identity.repository;

import com.group7.marketplacesystem.identity.entity.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuyerRepository extends JpaRepository<Buyer, Integer> {
    Optional<Buyer> getBuyerById(Integer id);
}
