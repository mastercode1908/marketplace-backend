package com.group7.marketplacesystem.promotion.service;

import com.group7.marketplacesystem.commerce.payment.entity.PaymentSession;
import com.group7.marketplacesystem.commerce.payment.response.VNPayUrlResponse;
import com.group7.marketplacesystem.promotion.dto.request.ServicePackageRequest;
import com.group7.marketplacesystem.promotion.dto.response.SellerPackageResponse;
import com.group7.marketplacesystem.promotion.dto.response.ServicePackageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ServicePackageService {
    ServicePackageResponse createServicePackage(ServicePackageRequest request, Integer adminId);
    ServicePackageResponse getServicePackageById(Integer id);
    Page<ServicePackageResponse> getAllServicePackages(Pageable pageable);
    ServicePackageResponse updateServicePackage(Integer id, ServicePackageRequest request);
    void deleteServicePackage(Integer id);
    VNPayUrlResponse buyServicePackageVNPAY(Integer id);
    List<SellerPackageResponse> getAllMyServicePackage();
    String createPackageUsageProduct(Integer productId);
    List<Integer> getPromotedProductIds();
    void updateExpiredPackages();
    void checkAndUpdateExpiredPackagesForSeller(Integer sellerId);

    void createOrUpdateSellerPackage(PaymentSession session);
}
