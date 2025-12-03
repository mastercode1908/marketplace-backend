package com.group7.marketplacesystem.identity.repository;

import com.group7.marketplacesystem.identity.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
}
