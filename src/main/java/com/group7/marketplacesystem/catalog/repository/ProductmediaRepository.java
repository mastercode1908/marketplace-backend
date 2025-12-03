package com.group7.marketplacesystem.catalog.repository;

import com.group7.marketplacesystem.catalog.entity.Productmedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ProductmediaRepository extends JpaRepository<Productmedia, Integer> {
    List<Productmedia> findByProductIdAndDeletedAtIsNull(Integer productId);

    List<Productmedia> findByProductId(Integer productId);
}