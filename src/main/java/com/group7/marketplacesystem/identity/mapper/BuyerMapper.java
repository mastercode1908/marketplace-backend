package com.group7.marketplacesystem.identity.mapper;

import com.group7.marketplacesystem.identity.dto.request.BuyerRequest;
import com.group7.marketplacesystem.identity.dto.request.UserRegisterRequest;
import com.group7.marketplacesystem.identity.dto.response.BuyerResponse;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BuyerMapper {
    public User toUserEntity(UserRegisterRequest request) {
        if (request == null) return null;

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        user.setRole("BUYER");
        user.setUserStatus("Pending");
        user.setEmailVerified(false);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setDeletedAt(null);
        return user;
    }

    public Buyer toBuyerEntity(User user) {
        if (user == null) return null;

        Buyer buyer = new Buyer();
        buyer.setUsers(user);
        buyer.setAddress(null);
        buyer.setDateOfBirth(null);

        return buyer;
    }

    public BuyerResponse toBuyerResponse(Buyer buyer) {
        if (buyer == null) return null;

        return BuyerResponse.builder()
                .buyerId(buyer.getId())
                .address(buyer.getAddress())
                .dateOfBirth(buyer.getDateOfBirth())
                
//                .deletedAt(buyer.getDeletedAt())

                .username(buyer.getUsers().getUsername())
                .email(buyer.getUsers().getEmail())
                .fullName(buyer.getUsers().getFullName())
                .phone(buyer.getUsers().getPhone())
                .gender(buyer.getUsers().getGender())
                .avatarUrl(buyer.getUsers().getAvatarUrl())
                .role(buyer.getUsers().getRole())
                .userStatus(buyer.getUsers().getUserStatus())
                .createdAt(buyer.getUsers().getCreatedAt())
                .build();
    }

    public Buyer toEntity(BuyerRequest request, User user) {
        if (request == null || user == null) return null;

        Buyer buyer = new Buyer();
        buyer.setUsers(user);
        buyer.setAddress(request.getAddress());
        buyer.setDateOfBirth(request.getDateOfBirth());

        return buyer;
    }
}
