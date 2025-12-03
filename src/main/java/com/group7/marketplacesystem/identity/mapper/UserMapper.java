package com.group7.marketplacesystem.identity.mapper;

import com.group7.marketplacesystem.identity.dto.request.UserRequest;
import com.group7.marketplacesystem.identity.dto.response.UserResponse;
import com.group7.marketplacesystem.identity.dto.response.UserToAdminResponse;
import com.group7.marketplacesystem.identity.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserMapper {
    // Từ request -> entity (khi tạo mới user)
    public User toEntity(UserRequest request) {
        if (request == null) return null;
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setRole(request.getRole() != null ? request.getRole() : "BUYER");
        user.setUserStatus("Active");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    // Từ entity -> response (trả về client)
    public UserResponse toResponse(User user) {
        if (user == null) return null;
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setGender(user.getGender());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setRole(user.getRole());
        response.setUserStatus(user.getUserStatus());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    // Cập nhật entity từ request (dùng khi update user)
    public void updateEntity(User user, UserRequest request) {
        if (user == null || request == null) return;
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getRole() != null) user.setRole(request.getRole());
        user.setUpdatedAt(Instant.now());
    }

    public UserToAdminResponse toUserToAdminResponse(User user) {
        if (user == null) return null;

        UserToAdminResponse response = new UserToAdminResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setGender(user.getGender());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setRole(user.getRole());
        response.setUserStatus(user.getUserStatus());
        response.setEmailVerified(user.getEmailVerified());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setDeletedAt(user.getDeletedAt());
        return response;
    }
}
