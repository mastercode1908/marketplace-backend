package com.group7.marketplacesystem.promotion.service.impl;

import com.group7.marketplacesystem.catalog.entity.Review;
import com.group7.marketplacesystem.commerce.payment.Service.VNPayService;
import com.group7.marketplacesystem.commerce.payment.entity.Payment;
import com.group7.marketplacesystem.commerce.payment.entity.PaymentSession;
import com.group7.marketplacesystem.commerce.payment.repository.PaymentRepository;
import com.group7.marketplacesystem.commerce.payment.repository.PaymentSessionRepository;
import com.group7.marketplacesystem.commerce.payment.response.VNPayUrlResponse;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.identity.entity.Admin;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.repository.AdminRepository;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.promotion.dto.request.ServicePackageRequest;
import com.group7.marketplacesystem.promotion.dto.response.SellerPackageResponse;
import com.group7.marketplacesystem.promotion.dto.response.ServicePackageResponse;
import com.group7.marketplacesystem.promotion.entity.PackageUsage;
import com.group7.marketplacesystem.promotion.entity.Sellerpackage;
import com.group7.marketplacesystem.promotion.entity.Servicepackage;
import com.group7.marketplacesystem.promotion.mapper.SellerPackageMapper;
import com.group7.marketplacesystem.promotion.mapper.ServicePackageMapper;
import com.group7.marketplacesystem.promotion.entity.Promotion;
import com.group7.marketplacesystem.promotion.repository.PackageUsageRepository;
import com.group7.marketplacesystem.promotion.repository.PromotionRepository;
import com.group7.marketplacesystem.promotion.repository.SellerPackageRepository;
import com.group7.marketplacesystem.promotion.repository.ServicePackageRepository;
import com.group7.marketplacesystem.promotion.service.ServicePackageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.group7.marketplacesystem.common.utils.VNPayUtils.getRandomNumber;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicePackageServiceImpl implements ServicePackageService {

    private final ServicePackageRepository servicePackageRepository;
    private final ServicePackageMapper servicePackageMapper;
    private final SellerRepository  sellerRepository;
    private final SellerPackageMapper  sellerPackageMapper;
    private final PaymentRepository paymentRepository;
    private final SellerPackageRepository sellerPackageRepository;
    private final PackageUsageRepository pkgUsageRepository;
    private final VNPayService vnPayService;
    private final PaymentSessionRepository paymentSessionRepository;
    private final PromotionRepository promotionRepository;

    @Override
    public ServicePackageResponse createServicePackage(ServicePackageRequest request, Integer adminId) {
        Servicepackage servicepackage = servicePackageMapper.toEntity(request, adminId);
        servicePackageRepository.save(servicepackage);
        return servicePackageMapper.toResponse(servicepackage);
    }

    @Override
    public ServicePackageResponse getServicePackageById(Integer id) {
        Servicepackage servicepackage = servicePackageRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND));
        return servicePackageMapper.toResponse(servicepackage);
    }

    @Override
    public Page<ServicePackageResponse> getAllServicePackages(Pageable pageable) {
        return servicePackageRepository.findAllByDeletedAtIsNull(pageable)
                .map(servicePackageMapper::toResponse);
    }

    @Override
    public ServicePackageResponse updateServicePackage(Integer id, ServicePackageRequest request) {
        Servicepackage servicepackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND));

        servicePackageMapper.updateEntity(servicepackage, request);
        servicePackageRepository.save(servicepackage);
        return servicePackageMapper.toResponse(servicepackage);
    }

    @Override
    public void deleteServicePackage(Integer id) {
        Servicepackage servicepackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND));
        servicepackage.setDeletedAt(Instant.now());
        servicePackageRepository.save(servicepackage);
    }

    @Override
    public VNPayUrlResponse buyServicePackageVNPAY(Integer id) {
        Servicepackage servicepackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        int sellerId = userDetails.getUser().getId();
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));


        // 3. Kiểm tra seller đã mua gói này còn Active hay chưa
        Optional<Sellerpackage> existingPackage = sellerPackageRepository
                .findBySellerIdAndPackageFieldIdAndStatus(sellerId, servicepackage.getId(), "Active");

        if (existingPackage.isPresent() && existingPackage.get().getEndDate().isAfter(Instant.now())) {
            throw new ApiException(ErrorCode.SERVICE_PACKAGE_STILL_ACTIVE);
        }

        // 5. Lưu vào payment_session
        PaymentSession session = new PaymentSession();
        session.setAmount(servicepackage.getPrice());
        session.setPaymentMethod("VNPAY");
        session.setStatus("PENDING");
        session.setCreatedAt(Instant.now());
        session.setTargetType("Package");
        session.setTargetId(servicepackage.getId());
        session.setSellerId(sellerId);

        String txnRef = getRandomNumber(8);
        session.setTxnRef(txnRef);

        session = paymentSessionRepository.save(session);


        String paymentUrl;
        try {
            paymentUrl = vnPayService.createPayment(servicepackage.getPrice(), txnRef);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return new VNPayUrlResponse(paymentUrl);
    }

    @Override
    public void createOrUpdateSellerPackage(PaymentSession session) {

        Integer sellerId = session.getSellerId();
        Integer packageId = session.getTargetId(); // gói đang mua
        if (packageId == null) {
            throw new ApiException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND);
        }

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        // Kiểm tra xem gói này của seller còn Active hay không
        Optional<Sellerpackage> existingPackage = sellerPackageRepository
                .findBySellerIdAndPackageFieldIdAndStatus(sellerId, packageId, "Active");

        if (existingPackage.isPresent() && existingPackage.get().getEndDate().isAfter(Instant.now())) {
            // Gói này vẫn còn hạn → không cho mua tiếp
            throw new ApiException(ErrorCode.SERVICE_PACKAGE_STILL_ACTIVE);
        }

        // Lấy duration từ ServicePackage
        Servicepackage sp = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new ApiException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND));

        int durationDays = sp.getDurationDays();

        Instant startDate = Instant.now();
        Instant endDate = startDate.plus(durationDays, ChronoUnit.DAYS);

        Payment payment = new Payment();
        payment.setTargetId(packageId);
        payment.setTargetType("Package");
        payment.setAmount(sp.getPrice());
        payment.setMethod("VNPAY");
        payment.setStatus("Paid");
        payment.setCreatedAt(Instant.now());
        paymentRepository.save(payment);

        // Tạo SellerPackage mới
        Sellerpackage sellerPackage = new Sellerpackage();
        sellerPackage.setSeller(seller);
        sellerPackage.setPackageField(sp);
        sellerPackage.setStartDate(startDate);
        sellerPackage.setEndDate(endDate);
        sellerPackage.setRemainingUsage(sp.getUsageLimit());
        sellerPackage.setCreatedAt(startDate);
        sellerPackage.setStatus("Active");

        sellerPackageRepository.save(sellerPackage);
    }


    @Override
    public List<SellerPackageResponse> getAllMyServicePackage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        int sellerId = userDetails.getUser().getId();
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        return sellerPackageRepository.findAllBySellerIs(seller)
                .stream()
                .filter(x -> "Active".equals(x.getStatus()))
                .map(sellerPackageMapper::toResponse)
                .toList();
    }

    @Override
    public String createPackageUsageProduct(Integer productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        int sellerId = userDetails.getUser().getId();
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        Sellerpackage sellerpackage = null;

        // Try ID 3 (Quảng Cáo Sản Phẩm)
        Optional<Servicepackage> sp3 = servicePackageRepository.findByIdAndDeletedAtIsNull(3);
        if (sp3.isPresent()) {
            sellerpackage = sellerPackageRepository.findByPackageFieldAndSellerAndStatus(sp3.get(), seller, "Active");
        }

        // Fallback to ID 2 (Content Extra) if not found
        if (sellerpackage == null) {
            Optional<Servicepackage> sp2 = servicePackageRepository.findByIdAndDeletedAtIsNull(2);
            if (sp2.isPresent()) {
                sellerpackage = sellerPackageRepository.findByPackageFieldAndSellerAndStatus(sp2.get(), seller, "Active");
            }
        }

        if (sellerpackage == null) {
            return "Bạn chưa đăng ký gói dịch vụ phù hợp hoặc gói đã hết hạn.";
        }

        if(sellerpackage.getRemainingUsage() == 0){
            return "Đã hết số lượt thêm sản phẩm vào mục quảng cáo tăng cường";
        }

        if(pkgUsageRepository.existsBySellerPackageAndTargetId(sellerpackage, productId)) {
            return "Sản phẩm đã được thêm vào mục quảng cáo tăng cường trước đó, vui lòng chọn sản phẩm khác";
        }

        PackageUsage  packageUsage = new PackageUsage();
        packageUsage.setSellerPackage(sellerpackage);
        packageUsage.setTargetId(productId);
        packageUsage.setTargetType("Product");
        packageUsage.setUsedAt(Instant.now());

        sellerpackage.setRemainingUsage(sellerpackage.getRemainingUsage() - 1);
        sellerPackageRepository.save(sellerpackage);

        pkgUsageRepository.save(packageUsage);
        return "Đã thêm sản phẩm vào mục quảng cáo tăng cường";
    }

    @Override
    public List<Integer> getPromotedProductIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        int sellerId = userDetails.getUser().getId();
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        List<Sellerpackage> activePackages = sellerPackageRepository.findAllBySellerIs(seller)
                .stream()
                .filter(x -> "Active".equals(x.getStatus()))
                .toList();

        List<Integer> productIds = new ArrayList<>();
        for (Sellerpackage pkg : activePackages) {
            List<PackageUsage> usages = pkgUsageRepository.findAllBySellerPackage(pkg);
            for (PackageUsage usage : usages) {
                if ("Product".equals(usage.getTargetType())) {
                    productIds.add(usage.getTargetId());
                }
            }
        }
        return productIds;
    }

    @Transactional
    public void updateExpiredPackages() {
        Instant now = Instant.now();
        List<Sellerpackage> activePackages = sellerPackageRepository.findAllByStatus("Active");
        
        log.info("Checking expired packages at {}. Found {} active packages", now, activePackages.size());

        List<Sellerpackage> expiredPackages = new ArrayList<>();
        List<Promotion> promotionsToExpire = new ArrayList<>();

        for (Sellerpackage sp : activePackages) {
            // Kiểm tra nếu end_date <= now (đã hết hạn hoặc hết hạn hôm nay)
            if (sp.getEndDate() != null && !sp.getEndDate().isAfter(now)) {
                log.info("Found expired package ID: {}, Seller ID: {}, End Date: {}, Current: {}", 
                    sp.getId(), sp.getSeller().getId(), sp.getEndDate(), now);
                
                // Chuyển status của package thành Expired
                sp.setStatus("Expired");
                expiredPackages.add(sp);
                
                // Lấy sellerId từ package
                Integer sellerId = sp.getSeller().getId();
                log.info("Processing seller ID: {} for package expiration", sellerId);
                
                // Tìm tất cả promotions của seller này (ownerType = "SELLER", ownerId = sellerId)
                // findAllActiveByIdUser trả về promotions chưa bị xóa (deleted_at IS NULL)
                List<Promotion> sellerPromotions = promotionRepository.findAllActiveByIdUser(sellerId);
                
                log.info("Found {} promotions for seller ID: {}", sellerPromotions.size(), sellerId);
                
                // Expire tất cả promotions của seller này (chỉ những cái chưa expired)
                for (Promotion promotion : sellerPromotions) {
                    // Kiểm tra ownerType = "SELLER" và ownerId = sellerId
                    if ("SELLER".equalsIgnoreCase(promotion.getOwnerType()) 
                            && promotion.getOwnerId().equals(sellerId)
                            && !"Expired".equalsIgnoreCase(promotion.getPromotionStatus())) {
                        log.info("Expiring promotion ID: {}, Code: {}, Current Status: {}", 
                            promotion.getId(), promotion.getPromotionCode(), promotion.getPromotionStatus());
                        // Chuyển status của promotion thành Expired
                        promotion.setPromotionStatus("Expired");
                        promotionsToExpire.add(promotion);
                    }
                }
            }
        }

        // Lưu tất cả packages đã expire
        if (!expiredPackages.isEmpty()) {
            sellerPackageRepository.saveAll(expiredPackages);
            sellerPackageRepository.flush(); // Đảm bảo lưu vào DB ngay lập tức
            log.info("Successfully updated {} expired packages in database", expiredPackages.size());
        } else {
            log.info("No expired packages found");
        }

        // Lưu tất cả promotions đã expire
        if (!promotionsToExpire.isEmpty()) {
            promotionRepository.saveAll(promotionsToExpire);
            promotionRepository.flush(); // Đảm bảo lưu vào DB ngay lập tức
            log.info("Successfully updated {} expired promotions in database", promotionsToExpire.size());
        } else {
            log.info("No expired promotions found");
        }
        
        log.info("Expiry check completed. Packages expired: {}, Promotions expired: {}", 
            expiredPackages.size(), promotionsToExpire.size());
    }

    @Override
    @Transactional
    public void checkAndUpdateExpiredPackagesForSeller(Integer sellerId) {
        Instant now = Instant.now();
        
        // Tìm tất cả active packages của seller này
        List<Sellerpackage> sellerPackages = sellerPackageRepository.findBySellerId(sellerId)
                .stream()
                .filter(sp -> "Active".equals(sp.getStatus()))
                .toList();
        
        log.info("Checking expired packages for seller ID: {}. Found {} active packages", sellerId, sellerPackages.size());

        List<Sellerpackage> expiredPackages = new ArrayList<>();
        List<Promotion> promotionsToExpire = new ArrayList<>();

        for (Sellerpackage sp : sellerPackages) {
            // Kiểm tra nếu end_date <= now (đã hết hạn hoặc hết hạn hôm nay)
            if (sp.getEndDate() != null && !sp.getEndDate().isAfter(now)) {
                log.info("Found expired package ID: {} for seller ID: {}, End Date: {}", 
                    sp.getId(), sellerId, sp.getEndDate());
                
                // Chuyển status của package thành Expired
                sp.setStatus("Expired");
                expiredPackages.add(sp);
                
                // Tìm tất cả promotions của seller này (ownerType = "SELLER", ownerId = sellerId)
                List<Promotion> sellerPromotions = promotionRepository.findAllActiveByIdUser(sellerId);
                
                log.info("Found {} promotions for seller ID: {}", sellerPromotions.size(), sellerId);
                
                // Expire tất cả promotions của seller này (chỉ những cái chưa expired)
                for (Promotion promotion : sellerPromotions) {
                    if ("SELLER".equalsIgnoreCase(promotion.getOwnerType()) 
                            && promotion.getOwnerId().equals(sellerId)
                            && !"Expired".equalsIgnoreCase(promotion.getPromotionStatus())) {
                        log.info("Expiring promotion ID: {}, Code: {}", promotion.getId(), promotion.getPromotionCode());
                        promotion.setPromotionStatus("Expired");
                        promotionsToExpire.add(promotion);
                    }
                }
            }
        }

        // Lưu tất cả packages đã expire
        if (!expiredPackages.isEmpty()) {
            sellerPackageRepository.saveAll(expiredPackages);
            sellerPackageRepository.flush();
            log.info("Updated {} expired packages for seller ID: {}", expiredPackages.size(), sellerId);
        }

        // Lưu tất cả promotions đã expire
        if (!promotionsToExpire.isEmpty()) {
            promotionRepository.saveAll(promotionsToExpire);
            promotionRepository.flush();
            log.info("Updated {} expired promotions for seller ID: {}", promotionsToExpire.size(), sellerId);
        }
    }
}
