package com.group7.marketplacesystem.catalog.repository;

import com.group7.marketplacesystem.catalog.entity.Productreport;
import com.group7.marketplacesystem.identity.entity.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReportRepository extends JpaRepository<Productreport, Integer> {
    List<Productreport> findByBuyerId(Integer buyerId);
}
