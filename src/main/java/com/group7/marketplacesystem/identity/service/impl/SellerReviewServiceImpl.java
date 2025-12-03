package com.group7.marketplacesystem.identity.service.impl;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.identity.dto.request.SellerRejectRequest;
import com.group7.marketplacesystem.identity.dto.response.SellerReviewResponse;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.identity.service.SellerReviewService;
import com.group7.marketplacesystem.infrastructure.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SellerReviewServiceImpl implements SellerReviewService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Override
    public Page<SellerReviewResponse> getPendingSellers(Pageable pageable) {
        return getSellersByStatus("Reviewing", pageable);
    }

    @Override
    public Page<SellerReviewResponse> getSellersByStatus(String status, Pageable pageable) {
        // Nếu status là ALL hoặc null/empty thì truyền null vào repository để lấy tất cả
        String searchStatus = (status == null || status.isEmpty() || "ALL".equalsIgnoreCase(status)) ? null : status;

        Page<User> users = userRepository.searchUsers(null, null, null, "SELLER", searchStatus, pageable);

        return users.map(user -> {
            Seller seller = sellerRepository.findById(user.getId())
                    .orElse(null);

            return SellerReviewResponse.builder()
                    .sellerId(user.getId())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .phone(user.getPhone())
                    .shopName(seller != null ? seller.getShopName() : null)
                    .shopAddress(seller != null ? seller.getShopAddress() : null)
                    .shopDescription(seller != null ? seller.getShopDescription() : null)
                    .taxCode(seller != null ? seller.getTaxCode() : null)
                    .userStatus(user.getUserStatus())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        });
    }

    @Override
    @Transactional
    public void approveSeller(Integer sellerId) {
        User user = userRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        if (!"SELLER".equals(user.getRole())) {
            throw new ApiException(ErrorCode.INVALID_ROLE);
        }

        if (!"Reviewing".equals(user.getUserStatus())) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }

        // Chuyển status sang Active
        user.setUserStatus("Active");
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        // Lấy thông tin seller để gửi email
        Seller seller = sellerRepository.findById(sellerId).orElse(null);
        String shopName = seller != null ? seller.getShopName() : null;

        // Gửi email thông báo duyệt
        mailService.sendSellerApprovalEmail(user.getEmail(), shopName);
    }

    @Override
    @Transactional
    public void rejectSeller(Integer sellerId, SellerRejectRequest request) {
        User user = userRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        if (!"SELLER".equals(user.getRole())) {
            throw new ApiException(ErrorCode.INVALID_ROLE);
        }

        if (!"Reviewing".equals(user.getUserStatus())) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }

        // Chuyển status về Incomplete
        user.setUserStatus("Incomplete");
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        // Lấy thông tin seller để gửi email
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));
        String shopName = seller.getShopName();

        // Gửi email thông báo từ chối (ghi chú chỉ gửi trong email, không lưu DB)
        mailService.sendSellerRejectionEmail(user.getEmail(), shopName, request.getNote());
    }
}

