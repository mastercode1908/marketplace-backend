package com.group7.marketplacesystem.identity.service.impl;


import com.group7.marketplacesystem.identity.dto.response.UserResponse;
import com.group7.marketplacesystem.identity.entity.Admin;
import com.group7.marketplacesystem.identity.mapper.UserMapper;
import com.group7.marketplacesystem.identity.repository.AdminRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import com.group7.marketplacesystem.catalog.mapper.ProductMapper;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.commerce.order.mapper.OrderMapper;
import com.group7.marketplacesystem.commerce.order.repository.OrderRepository;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.identity.dto.response.UserDetailResponse;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.catalog.mapper.ProductReportMapper;
import com.group7.marketplacesystem.catalog.repository.ProductReportRepository;
import com.group7.marketplacesystem.infrastructure.mapper.SystemlogMapper;
import com.group7.marketplacesystem.infrastructure.repository.SystemlogRepository;
import com.group7.marketplacesystem.promotion.mapper.SellerPackageMapper;
import com.group7.marketplacesystem.promotion.repository.SellerPackageRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;
    private final AdminRepository adminRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final SystemlogRepository systemlogRepository;
    private final SystemlogMapper systemlogMapper;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final SellerPackageRepository sellerPackageRepository;
    private final SellerPackageMapper sellerPackageMapper;
    private final ProductReportRepository productReportRepository;
    private final ProductReportMapper productReportMapper;


    @Override
    public Page<UserResponse> searchUsers(String username, String email, String phone, String role, String userStatus, Pageable pageable) {
        return userRepository.searchUsers(username, email, phone, role, userStatus, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    public UserDetailResponse getUserDetails(Integer userId) {
        User user = userRepository.findByIdByDeletedAtIsNull(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        UserDetailResponse response = new UserDetailResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setFullName(user.getFullName());
        response.setGender(user.getGender());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setRole(user.getRole());
        response.setUserStatus(user.getUserStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        // Fetch common related data
        response.setSystemLogs(systemlogRepository.findByUserId(userId).stream()
                .map(systemlogMapper::toResponse)
                .collect(Collectors.toList()));

        // Fetch role-specific data
        if ("BUYER".equals(user.getRole())) {
            Buyer buyer = buyerRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));
            response.setAddress(buyer.getAddress());
            response.setDateOfBirth(buyer.getDateOfBirth());
            response.setOrderHistory(orderRepository.findByBuyerId(userId).stream()
                    .map(orderMapper::toResponse)
                    .collect(Collectors.toList()));
            response.setProductReports(productReportRepository.findByBuyerId(userId).stream()
                    .map(productReportMapper::toResponse)
                    .collect(Collectors.toList()));
        } else if ("SELLER".equals(user.getRole())) {
            Seller seller = sellerRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));
            response.setShopName(seller.getShopName());
            response.setShopAddress(seller.getShopAddress());
            response.setShopDescription(seller.getShopDescription());
            response.setTaxCode(seller.getTaxCode());
            response.setRatingCount(seller.getRatingCount());
            response.setTotalSales(seller.getTotalSales());
            response.setProducts(productRepository.findBySellerId(userId).stream()
                    .map(productMapper::toResponse)
                    .collect(Collectors.toList()));
            response.setSellerPackages(sellerPackageRepository.findBySellerId(userId).stream()
                    .map(sellerPackageMapper::toResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    @Override
    public UserResponse updateUserStatus(Integer userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));
        user.setUserStatus(status);
        userRepository.save(user);
        return userMapper.toResponse(user);

    }


    @Override
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        user.setDeletedAt(Instant.now());
        user.setUserStatus("Inactive");
        userRepository.save(user);
    }

    @Override
    public List<UserResponse> searchUsersByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.findByUsernameContainingOrEmailContaining(searchTerm.trim())
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getAdminIdByRole(String role) {
        List<User> admins = userRepository.findAllByRole(role);
        if (admins.isEmpty()) {
            throw new ApiException(ErrorCode.USER_NOT_EXISTED);
        }
        // Trả về admin đầu tiên có role này
        return admins.get(0).getId();
    }
}
