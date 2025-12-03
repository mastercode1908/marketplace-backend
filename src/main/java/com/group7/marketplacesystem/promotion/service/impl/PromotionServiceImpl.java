package com.group7.marketplacesystem.promotion.service.impl;

import com.group7.marketplacesystem.commerce.cart.entity.Cart;
import com.group7.marketplacesystem.commerce.cart.entity.Cartitem;
import com.group7.marketplacesystem.commerce.cart.repository.CartItemRepository;
import com.group7.marketplacesystem.commerce.cart.repository.CartRepository;
import com.group7.marketplacesystem.commerce.order.entity.Order;
import com.group7.marketplacesystem.commerce.order.repository.OrderRepository;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.security.CurrentUser;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.promotion.dto.request.PromotionApplyRequest;
import com.group7.marketplacesystem.promotion.dto.request.PromotionRequest;
import com.group7.marketplacesystem.promotion.dto.response.PromotionApplyResponse;
import com.group7.marketplacesystem.promotion.dto.response.PromotionResponse;
import com.group7.marketplacesystem.promotion.entity.Promotion;
import com.group7.marketplacesystem.promotion.entity.Promotionusage;
//import com.group7.marketplacesystem.promotion.entity.PromotionusageId;
import com.group7.marketplacesystem.promotion.mapper.PromotionMapper;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.promotion.entity.Sellerpackage;
import com.group7.marketplacesystem.promotion.entity.Servicepackage;
import com.group7.marketplacesystem.promotion.repository.PromotionRepository;
import com.group7.marketplacesystem.promotion.repository.PromotionUsageRepository;
import com.group7.marketplacesystem.promotion.repository.SellerPackageRepository;
import com.group7.marketplacesystem.promotion.repository.ServicePackageRepository;
import com.group7.marketplacesystem.promotion.service.PromotionService;
import com.group7.marketplacesystem.promotion.service.ServicePackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final UserRepository userRepository;


    private final PromotionUsageRepository usageRepository;
    private final BuyerRepository buyerRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final SellerRepository sellerRepository;
    private final SellerPackageRepository sellerPackageRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final ServicePackageService servicePackageService;


    @Override
    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request, Integer userId) {
        // Validate phạm vi ngày
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException(ErrorCode.INVALID_DATE_RANGE);
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new ApiException(ErrorCode.INVALID_START_DATE);
        }

        if (request.getEndDate().isBefore(LocalDate.now())) {
            throw new ApiException(ErrorCode.INVALID_END_DATE);
        }

        // Check mã code tồn tại hay chưa
        if (promotionRepository.existsByPromotionCodeAndNotDeleted(request.getPromotionCode().trim()) == 1) {
            throw new ApiException(ErrorCode.PROMOTION_CODE_EXISTS);
        }

        // check nếu chọn kiểu % thì giá trị giảm phải luôn < 100
        if ("PERCENT".equals(request.getDiscountType()) && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ApiException(ErrorCode.INVALID_DISCOUNT_PERCENT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        // Map request to entity
        Promotion promotion = promotionMapper.toEntity(request, user);

        // lưu vào database
        Promotion savedPromotion = promotionRepository.save(promotion);

        return promotionMapper.toResponse(savedPromotion);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(Integer id) {
        Promotion promotion = promotionRepository.findByIdActive(id)
                .orElseThrow(() -> new ApiException(ErrorCode.PROMOTION_NOT_FOUND));

        return promotionMapper.toResponse(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionResponse> getAllPromotions(Pageable pageable) {
        return promotionRepository.findAllActive(pageable).map(promotionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionResponse> getPromotionsByIdUser(Pageable pageable, Integer userId) {
        return promotionRepository.findAllActiveByIdUser(pageable, userId).map(promotionMapper::toResponse);
    }

    @Override
    @Transactional
    public PromotionResponse updatePromotion(Integer id, PromotionRequest request) {
        // Validate phạm vi ngày
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException(ErrorCode.INVALID_DATE_RANGE);
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new ApiException(ErrorCode.INVALID_START_DATE);
        }

        if (request.getEndDate().isBefore(LocalDate.now())) {
            throw new ApiException(ErrorCode.INVALID_END_DATE);
        }

        // tìm kiếm promotion có trùng id
        Promotion existingPromotion = promotionRepository.findByIdActive(id)
                .orElseThrow(() -> new ApiException(ErrorCode.PROMOTION_NOT_FOUND));

        // nếu mã code thay đổi đã có mã code trùng trong tương tự thì sẽ báo lỗi
        if (!existingPromotion.getPromotionCode().equals(request.getPromotionCode().trim())) {
            if (promotionRepository.existsByPromotionCodeAndIdNotAndNotDeleted(request.getPromotionCode(), id) == 1) {
                throw new ApiException(ErrorCode.PROMOTION_CODE_EXISTS);
            }
        }

        // check nếu chọn kiểu % thì giá trị giảm phải luôn < 100
        if ("PERCENT".equals(request.getDiscountType()) && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ApiException(ErrorCode.INVALID_DISCOUNT_PERCENT);
        }

        // update entity với data mới
        promotionMapper.updateEntity(existingPromotion, request);

        // lưu update mới
        Promotion updatedPromotion = promotionRepository.save(existingPromotion);

        return promotionMapper.toResponse(updatedPromotion);
    }

    @Override
    @Transactional
    public void deletePromotion(Integer id) {
        // Tìm promotion
        Promotion promotion = promotionRepository.findByIdActive(id)
                .orElseThrow(() -> new ApiException(ErrorCode.PROMOTION_NOT_FOUND));

        // chèn vào cột delete_at ngày hện tại
        promotion.setDeletedAt(Instant.now());
        promotionRepository.save(promotion);
    }

    @Override
    @Transactional
    public void expirePromotions() {
        LocalDate now = LocalDate.now();
        List<Promotion> promotions = promotionRepository.findByEndDateBeforeAndDeletedAtIsNull(now);
        promotions.forEach(p -> {
            p.setDeletedAt(Instant.now());
        });

        promotionRepository.saveAll(promotions);
    }






    @Override
    public PromotionApplyResponse previewPromotion(PromotionApplyRequest req) {
        // Find promotion by code and not deleted
        Promotion promotion = promotionRepository.findByPromotionCode(req.getCode())
                .orElseThrow(() -> new ApiException(ErrorCode.PROMOTION_NOT_FOUND));

        if (promotion.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.PROMOTION_NOT_FOUND); // coi như đã xóa
        }

        // Lấy ownerId trực tiếp từ promotion
        Integer ownerId = promotion.getOwnerId();
        String ownerType = promotion.getOwnerType();

        // -------------------
        // Check promotion status
        // -------------------
        if (!"Active".equalsIgnoreCase(promotion.getPromotionStatus())) {
            throw new ApiException(ErrorCode.PROMOTION_INACTIVE);
        }

        Integer buyerId = CurrentUser.getUserId();
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_NOT_FOUND));

        List<Cartitem> cartItems = cartItemRepository.findByCartId(cart.getId());

        // -------------------
        // Check owner type
        // -------------------
        if ("SELLER".equalsIgnoreCase(ownerType)) {
            if (!ownerId.equals(req.getSellerId())) {
                throw new ApiException(ErrorCode.PROMOTION_WRONG_SELLER);
            }
        }

        // -------------------
        // Check start and end date
        // -------------------
        LocalDate today = LocalDate.now();

        LocalDate start = promotion.getStartDate();
        LocalDate end = promotion.getEndDate();

        if (start != null && today.isBefore(start)) {
            throw new ApiException(ErrorCode.PROMOTION_NOT_STARTED);
        }

        if (end != null && today.isAfter(end)) {
            throw new ApiException(ErrorCode.PROMOTION_EXPIRED);
        }

        // -------------------
        // Check usage limit
        // -------------------
        if (promotion.getUsageLimit() != null && promotion.getUsageLimit() <= 0) {
            throw new ApiException(ErrorCode.PROMOTION_USAGE_EXCEEDED);
        }

        // -------------------
        // Calculate discount
        // -------------------
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal orderTotal = req.getOrderTotal();

        if ("PERCENT".equalsIgnoreCase(promotion.getDiscountType())) {
            discount = orderTotal
                    .multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if ("AMOUNT".equalsIgnoreCase(promotion.getDiscountType())) {
            discount = promotion.getDiscountValue();
        }

        if (promotion.getMaxDiscountAmount() != null) {
            discount = discount.min(promotion.getMaxDiscountAmount());
        }

        // NEW: Không cho discount vượt quá orderTotal
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }
        // -------------------
        // Return response (chỉ discountAmount)
        // -------------------
        return PromotionApplyResponse.builder()
                .code(req.getCode())
                .discountAmount(discount)
                .message("Promotion preview successful")
                .build();
    }
    // ========================
    //  SAVE USAGE SAU KHI ĐẶT HÀNG
    // ========================
    @Override
    public void savePromotionUsage(Integer promotionId, Integer buyerId, Integer orderId) {

        // Lấy promotion, buyer, order từ DB
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROMOTION_NOT_FOUND));

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));

        // Tăng usedCount và save promotion
        promotion.setUsedCount(promotion.getUsedCount() + 1);
        promotion.setUsageLimit(promotion.getUsageLimit() - 1);
        promotionRepository.save(promotion);


        // Tạo entity Promotionusage
        Promotionusage usage = new Promotionusage();
        usage.setPromotion(promotion);
        usage.setBuyer(buyer);
        usage.setOrder(order);
        usage.setUsedAt(Instant.now());

        // Lưu vào DB
        usageRepository.save(usage);
    }

    // ========================
    //  SELLER PROMOTION METHODS
    // ========================

    /**
     * kiểm tra và xác thực gói của người bán để tạo mã giảm giá
     */
    private Sellerpackage validateSellerPackage(Integer sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        List<Promotion> promotions = promotionRepository.findAllActiveByIdUser(sellerId);

        // Get package with id = 1 (discount code package)
        Servicepackage discountCodePackage = servicePackageRepository.findByIdAndDeletedAtIsNull(2)
                .orElseThrow(() -> new ApiException(ErrorCode.SERVICE_PACKAGE_NOT_FOUND));

        // Find seller's package
        Sellerpackage sellerPackage = sellerPackageRepository
                .findByPackageFieldAndSellerAndStatus(discountCodePackage, seller, "Active");

        if (sellerPackage == null) {
            throw new ApiException(ErrorCode.PACKAGE_NOT_PURCHASED);
        }

        // Check if package is expired (current date > end_date)
        Instant now = Instant.now();
        if (sellerPackage.getEndDate() != null && sellerPackage.getEndDate().isBefore(now)) {
            sellerPackage.setStatus("Expired");

            for (Promotion promotion : promotions) {
                promotion.setPromotionStatus("Expired");
            }
            promotionRepository.saveAll(promotions);
            sellerPackageRepository.save(sellerPackage);
            throw new ApiException(ErrorCode.PACKAGE_EXPIRED);
        }

        // Check if status is already Expired
        if ("Expired".equalsIgnoreCase(sellerPackage.getStatus())) {
            throw new ApiException(ErrorCode.PACKAGE_EXPIRED);
        }

        // Check remaining usage
        if (sellerPackage.getRemainingUsage() == null || sellerPackage.getRemainingUsage() <= 0) {
            sellerPackage.setStatus("Expired");
            sellerPackageRepository.save(sellerPackage);
            throw new ApiException(ErrorCode.PACKAGE_USAGE_EXCEEDED);
        }

        return sellerPackage;
    }

    @Override
    @Transactional
    public PromotionResponse createSellerPromotion(PromotionRequest request, Integer sellerId) {
        // Validate package first
        Sellerpackage sellerPackage = validateSellerPackage(sellerId);

        // Validate phạm vi ngày
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException(ErrorCode.INVALID_DATE_RANGE);
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new ApiException(ErrorCode.INVALID_START_DATE);
        }

        if (request.getEndDate().isBefore(LocalDate.now())) {
            throw new ApiException(ErrorCode.INVALID_END_DATE);
        }

        // Check mã code tồn tại hay chưa
        if (promotionRepository.existsByPromotionCodeAndNotDeleted(request.getPromotionCode().trim()) == 1) {
            throw new ApiException(ErrorCode.PROMOTION_CODE_EXISTS);
        }

        // check nếu chọn kiểu % thì giá trị giảm phải luôn < 100
        if ("PERCENT".equals(request.getDiscountType()) && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ApiException(ErrorCode.INVALID_DISCOUNT_PERCENT);
        }

        if (request.getUsageLimit() > 10) {
            throw new ApiException(ErrorCode.INVALID_QUANTITY_PROMOTION);
        }

        User user = userRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        // Map request to entity
        Promotion promotion = promotionMapper.toEntity(request, user);

        // lưu vào database
        Promotion savedPromotion = promotionRepository.save(promotion);

        // Decrease remaining_usage by 1
        sellerPackage.setRemainingUsage(sellerPackage.getRemainingUsage() - 1);

        // If remaining_usage reaches 0, set status to Expired
        if (sellerPackage.getRemainingUsage() <= 0) {
            sellerPackage.setStatus("Expired");
        }

        sellerPackageRepository.save(sellerPackage);

        return promotionMapper.toResponse(savedPromotion);
    }

    @Override
    @Transactional
    public PromotionResponse updateSellerPromotion(Integer id, PromotionRequest request, Integer sellerId) {
        // Validate package first
        validateSellerPackage(sellerId);

        // Validate phạm vi ngày
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException(ErrorCode.INVALID_DATE_RANGE);
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new ApiException(ErrorCode.INVALID_START_DATE);
        }

        if (request.getEndDate().isBefore(LocalDate.now())) {
            throw new ApiException(ErrorCode.INVALID_END_DATE);
        }

        // tìm kiếm promotion có trùng id và thuộc về seller này
        Promotion existingPromotion = promotionRepository.findByIdActive(id)
                .orElseThrow(() -> new ApiException(ErrorCode.PROMOTION_NOT_FOUND));

        // Check if promotion belongs to this seller
        if (!"SELLER".equalsIgnoreCase(existingPromotion.getOwnerType()) 
                || !existingPromotion.getOwnerId().equals(sellerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (request.getUsageLimit() > 10) {
            throw new ApiException(ErrorCode.INVALID_QUANTITY_PROMOTION);
        }

        // nếu mã code thay đổi đã có mã code trùng trong tương tự thì sẽ báo lỗi
        if (!existingPromotion.getPromotionCode().equals(request.getPromotionCode().trim())) {
            if (promotionRepository.existsByPromotionCodeAndIdNotAndNotDeleted(request.getPromotionCode(), id) == 1) {
                throw new ApiException(ErrorCode.PROMOTION_CODE_EXISTS);
            }
        }

        // check nếu chọn kiểu % thì giá trị giảm phải luôn < 100
        if ("PERCENT".equals(request.getDiscountType()) && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ApiException(ErrorCode.INVALID_DISCOUNT_PERCENT);
        }

        // update entity với data mới
        promotionMapper.updateEntity(existingPromotion, request);

        // lưu update mới
        Promotion updatedPromotion = promotionRepository.save(existingPromotion);


        return promotionMapper.toResponse(updatedPromotion);
    }

    @Override
    @Transactional
    public void deleteSellerPromotion(Integer id, Integer sellerId) {
        // Validate package first
        validateSellerPackage(sellerId);

        // Tìm promotion
        Promotion promotion = promotionRepository.findByIdActive(id)
                .orElseThrow(() -> new ApiException(ErrorCode.PROMOTION_NOT_FOUND));

        // Check if promotion belongs to this seller
        if (!"SELLER".equalsIgnoreCase(promotion.getOwnerType()) 
                || !promotion.getOwnerId().equals(sellerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        // chèn vào cột delete_at ngày hện tại
        promotion.setDeletedAt(Instant.now());
        promotionRepository.save(promotion);
    }

    @Override
    @Transactional
    public Page<PromotionResponse> getSellerPromotions(Pageable pageable, Integer sellerId) {
        // Check và update expired packages và promotions của seller này trước khi trả về
        servicePackageService.checkAndUpdateExpiredPackagesForSeller(sellerId);
        
        // Trả về danh sách promotions (không cần validate package vì seller vẫn có thể xem promotions của họ)
        return promotionRepository.findAllActiveByIdUser(pageable, sellerId).map(promotionMapper::toResponse);
    }
}
