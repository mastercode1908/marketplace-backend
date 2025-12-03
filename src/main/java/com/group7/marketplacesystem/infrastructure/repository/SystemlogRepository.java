package com.group7.marketplacesystem.infrastructure.repository;

import com.group7.marketplacesystem.infrastructure.entity.Systemlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemlogRepository extends JpaRepository<Systemlog, Integer> {
    List<Systemlog> findByUserId(Integer userId);
}
