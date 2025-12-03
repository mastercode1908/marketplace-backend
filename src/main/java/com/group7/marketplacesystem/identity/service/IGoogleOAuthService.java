package com.group7.marketplacesystem.identity.service;

import com.group7.marketplacesystem.identity.dto.response.AuthResponse;

public interface IGoogleOAuthService {
    String buildGoogleAuthUrl(String role);
    AuthResponse handleGoogleCallback(String code, String role);

    String buildGoogleAuthUrlLogin();}
