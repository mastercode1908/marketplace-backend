package com.group7.marketplacesystem.promotion.repository;

import com.group7.marketplacesystem.promotion.entity.Sellerpackage;
import com.group7.marketplacesystem.promotion.entity.Servicepackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicePackageRepository extends JpaRepository<Servicepackage, Integer> {

    Page<Servicepackage> findAllByDeletedAtIsNull(Pageable pageable);
    Optional<Servicepackage> findByIdAndDeletedAtIsNull(Integer id);
}
