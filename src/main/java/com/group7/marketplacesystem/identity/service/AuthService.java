package com.group7.marketplacesystem.identity.service;

import com.group7.marketplacesystem.identity.dto.request.AuthRequest;
import com.group7.marketplacesystem.identity.dto.request.RefreshTokenRequest;
import com.group7.marketplacesystem.identity.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(AuthRequest request);
    AuthResponse refreshToken(String refreshToken);
    void changePassword(com.group7.marketplacesystem.identity.dto.request.ChangePasswordRequest request);
}
