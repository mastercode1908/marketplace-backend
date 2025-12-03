package com.group7.marketplacesystem.identity.service;

import com.group7.marketplacesystem.identity.dto.request.UserRegisterRequest;
import com.group7.marketplacesystem.identity.dto.response.AuthResponse;

public interface RegisterService {
    AuthResponse registerUser(UserRegisterRequest request);
}
