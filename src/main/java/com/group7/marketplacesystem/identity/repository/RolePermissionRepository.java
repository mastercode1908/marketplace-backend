package com.group7.marketplacesystem.identity.repository;

import com.group7.marketplacesystem.identity.entity.RolePermission;
import com.group7.marketplacesystem.identity.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    @Query("SELECT rp.permission.code FROM RolePermission rp WHERE UPPER(rp.id.role) = UPPER(:role) AND rp.deletedAt IS NULL")
    List<String> findPermissionCodesByRole(@Param("role") String role);
}