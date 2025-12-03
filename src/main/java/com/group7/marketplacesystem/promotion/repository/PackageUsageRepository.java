package com.group7.marketplacesystem.promotion.repository;

import com.group7.marketplacesystem.promotion.entity.PackageUsage;
import com.group7.marketplacesystem.promotion.entity.Sellerpackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageUsageRepository extends JpaRepository<PackageUsage, Integer> {

    boolean existsBySellerPackageAndTargetId(Sellerpackage sellerPackage, Integer targetId);
    List<PackageUsage> findAllTargetIdBySellerPackageIn(List<Sellerpackage> sellerPackages);
    List<PackageUsage> findAllBySellerPackage(Sellerpackage sellerPackage);
}

