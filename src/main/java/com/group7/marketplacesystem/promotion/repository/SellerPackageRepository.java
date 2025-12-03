package com.group7.marketplacesystem.promotion.repository;

import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.promotion.entity.Sellerpackage;
import com.group7.marketplacesystem.promotion.entity.Servicepackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerPackageRepository extends JpaRepository<Sellerpackage, Integer> {
    List<Sellerpackage> findBySellerId(Integer sellerId);
    List<Sellerpackage> findAllBySellerIs(Seller seller);
    Sellerpackage findByPackageFieldAndSellerAndStatus(Servicepackage servicePackage, Seller seller, String status);
    List<Sellerpackage> findAllByStatus(String status);
    List<Sellerpackage> findAllIdsByPackageFieldAndStatus(Servicepackage servicepackage, String status);
    List<Sellerpackage> findAllSellersByPackageField(Servicepackage servicepackage);

    Optional<Sellerpackage> findBySellerIdAndPackageFieldIdAndStatus(Integer sellerId, Integer packageId, String status);
}
