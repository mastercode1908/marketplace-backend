package com.group7.marketplacesystem.identity.mapper;

import com.group7.marketplacesystem.identity.dto.response.AuthResponse;
import com.group7.marketplacesystem.identity.dto.response.AuthUserInfo;
import com.group7.marketplacesystem.identity.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    public AuthResponse toAuthResponse(User user, String accessToken, String refreshToken) {
        if (user == null) return null;

        // Map thông tin user sang AuthUserInfo
        AuthUserInfo userInfo = new AuthUserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setFullName(user.getFullName());
        userInfo.setRole(user.getRole());
        userInfo.setAvatarUrl(user.getAvatarUrl());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setUser(userInfo);

        // 🔹 Xác định status an toàn
        if ("Incomplete".equalsIgnoreCase(user.getUserStatus())) {
            response.setStatus("INCOMPLETE");
            response.setMessage("Bạn cần hoàn thiện thông tin trước khi sử dụng hệ thống.");
        } else {
            response.setStatus("ACTIVE");
            response.setMessage("Đăng nhập thành công!");
        }

        return response;
    }

}
