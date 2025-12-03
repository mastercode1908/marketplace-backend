package com.group7.marketplacesystem.promotion.mapper;

import com.group7.marketplacesystem.identity.entity.Admin;
import com.group7.marketplacesystem.promotion.dto.request.ServicePackageRequest;
import com.group7.marketplacesystem.promotion.dto.response.ServicePackageResponse;
import com.group7.marketplacesystem.promotion.entity.Servicepackage;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ServicePackageMapper {

    public Servicepackage toEntity(ServicePackageRequest request, Integer adminId) {
        Servicepackage servicepackage = new Servicepackage();
        servicepackage.setName(request.getName().trim());
        servicepackage.setType(request.getType().trim());
        servicepackage.setDescription(request.getDescription().trim());
        servicepackage.setPrice(request.getPrice());
        servicepackage.setDurationDays(request.getDurationDays());
        servicepackage.setUsageLimit(request.getUsageLimit());
        servicepackage.setCreatedAt(Instant.now());
        servicepackage.setUpdatedAt(Instant.now());

        Admin admin = new Admin();
        admin.setId(adminId);
        servicepackage.setAdmin(admin);

        return servicepackage;
    }

    public ServicePackageResponse toResponse(Servicepackage servicepackage) {
        return ServicePackageResponse.builder()
                .id(servicepackage.getId())
                .adminId(servicepackage.getAdmin().getId())
                .name(servicepackage.getName().trim())
                .type(servicepackage.getType().trim())
                .description(servicepackage.getDescription().trim())
                .price(servicepackage.getPrice())
                .durationDays(servicepackage.getDurationDays())
                .usageLimit(servicepackage.getUsageLimit())
                .createdAt(servicepackage.getCreatedAt())
                .updatedAt(servicepackage.getUpdatedAt())
                .deletedAt(servicepackage.getDeletedAt())
                .build();
    }

    public void updateEntity(Servicepackage servicepackage, ServicePackageRequest request) {
        servicepackage.setName(request.getName().trim());
        servicepackage.setType(request.getType().trim());
        servicepackage.setDescription(request.getDescription().trim());
        servicepackage.setPrice(request.getPrice());
        servicepackage.setDurationDays(request.getDurationDays());
        servicepackage.setUsageLimit(request.getUsageLimit());
        servicepackage.setUpdatedAt(Instant.now());
    }
}
