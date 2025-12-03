package com.group7.marketplacesystem.identity.mapper;

import com.group7.marketplacesystem.identity.dto.request.ShopInfoRequest;
import com.group7.marketplacesystem.identity.dto.request.UserRegisterRequest;
import com.group7.marketplacesystem.identity.dto.response.SellerResponse;
import com.group7.marketplacesystem.identity.dto.response.ShopInfoResponse;
import com.group7.marketplacesystem.identity.dto.response.UserToAdminResponse;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SellerMapper {

    private final UserMapper userMapper;

    public User toUserEntity(UserRegisterRequest request) {
        if (request == null) return null;

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        user.setRole("SELLER");
        user.setUserStatus("Pending");
        user.setEmailVerified(false);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setDeletedAt(null);
        return user;
    }

    public Seller toSellerEntity(User user) {
        if (user == null) return null;

        Seller seller = new Seller();
        seller.setUsers(user);
        seller.setTaxCode(null);
        seller.setShopName(null);
        seller.setShopAddress(null);
        seller.setShopDescription(null);
        seller.setRatingCount(0);
        seller.setTotalSales(0);

        return seller;
    }

    public SellerResponse toSellerResponse(Seller seller) {
        if (seller == null || seller.getUsers() == null) return null;

        User user = seller.getUsers();

        return SellerResponse.builder()
                .user(SellerResponse.UserInfo.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .full_name(user.getFullName())
                        .gender(user.getGender() == null ? null :
                                user.getGender() ? "Male" : "Female")
                        .phone(user.getPhone())
                        .avatar(user.getAvatarUrl())
                        .build()
                )
                .seller(SellerResponse.SellerInfo.builder()
                        .shop_name(seller.getShopName())
                        .shop_address(seller.getShopAddress())
                        .shop_description(seller.getShopDescription())
                        .taxCode(seller.getTaxCode()) // read-only
                        .rating_count(seller.getRatingCount())
                        .total_sales(seller.getTotalSales())
                        .build()
                )
                .build();
    }

    public Seller updateSellerFromRequest(Seller seller, ShopInfoRequest request) {
        if (seller == null || request == null) return seller;

        // Chỉ cập nhật những field shop info
        seller.setShopName(request.getShopName());
        seller.setShopAddress(request.getShopAddress());
        seller.setShopDescription(request.getShopDescription());
        seller.setTaxCode(request.getTaxCode());

        // Không chạm vào ratingCount, totalSales hay user liên kết
        return seller;
    }

}
