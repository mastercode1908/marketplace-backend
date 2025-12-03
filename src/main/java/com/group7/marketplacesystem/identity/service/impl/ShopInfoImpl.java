package com.group7.marketplacesystem.identity.service.impl;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.identity.dto.request.ShopInfoRequest;
import com.group7.marketplacesystem.identity.dto.response.ShopInfoResponse;
import com.group7.marketplacesystem.identity.dto.response.UserToAdminResponse;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.mapper.SellerMapper;
import com.group7.marketplacesystem.identity.mapper.UserMapper;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.identity.service.ShopInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopInfoImpl implements ShopInfoService {
    private final SellerMapper sellerMapper;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper; // mapper chuyển User -> UserToAdminResponse

    @Override
    public void postShopInfo(ShopInfoRequest request){
        // Kiểm tra xem seller đã tồn tại chưa
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // Hoặc lấy userId tuỳ config

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        if (!"SELLER".equals(user.getRole()) || !"Incomplete".equals(user.getUserStatus())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (request.getTaxCode() != null && !request.getTaxCode().isBlank()) {
            boolean exists = sellerRepository.existsByTaxCode(request.getTaxCode());
            if (exists) {
                throw new ApiException(ErrorCode.TAX_EXISTED);
            }
        }

        // Kiểm tra seller
        Seller seller = sellerRepository.findById(user.getId())
                .orElse(null);

        // Tạo hoặc update Seller entity
        // Nếu CHƯA có seller → tạo mới
        if (seller == null) {
            seller = sellerMapper.toSellerEntity(user);
        }

        // Update shop info
        sellerMapper.updateSellerFromRequest(seller, request);

        sellerRepository.save(seller);

        // Cập nhật userStatus sang Reviewing nếu chưa phải Reviewing
        if (!"Reviewing".equalsIgnoreCase(user.getUserStatus())) {
            user.setUserStatus("Reviewing");
            userRepository.save(user);
        }
    }
}
