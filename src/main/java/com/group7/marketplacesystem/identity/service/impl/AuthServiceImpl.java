package com.group7.marketplacesystem.identity.service.impl;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.security.CustomUserDetails;
import com.group7.marketplacesystem.common.security.JwtUtils;
import com.group7.marketplacesystem.identity.dto.request.AuthRequest;
import com.group7.marketplacesystem.identity.dto.request.RefreshTokenRequest;
import com.group7.marketplacesystem.identity.dto.response.AuthResponse;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.mapper.AuthMapper;
import com.group7.marketplacesystem.identity.repository.RolePermissionRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.identity.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor

public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final AuthMapper authMapper; // 👈 thêm vào
    private final RolePermissionRepository rolePermissionRepository;


    @Override
    public AuthResponse login(AuthRequest request) {


        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        // 🔹 Check nếu user đã bị xóa mềm
        if (user.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.ACCOUNT_DELETED);
        }

        // 🔹 Check trạng thái trước khi xác thực password
        if (!user.getEmailVerified()) {
            throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        if ("Banned".equalsIgnoreCase(user.getUserStatus())) {
            throw new ApiException(ErrorCode.ACCOUNT_BANNED);
        }
        if ("Inactive".equalsIgnoreCase(user.getUserStatus())) {
            throw new ApiException(ErrorCode.ACCOUNT_INACTIVE);
        }
        if ("Pending".equalsIgnoreCase(user.getUserStatus())) {
            throw new ApiException(ErrorCode.ACCOUNT_PENDING);
        }
        if("Reviewing".equalsIgnoreCase(user.getUserStatus())) {
            throw new ApiException(ErrorCode.ACCOUNT_REVIEWING);
        }

        // Xác thực user bằng AuthenticationManager tu dong ca email va password khac ben google tra ve access tokem
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Lấy CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Lấy entity User từ CustomUserDetails
//        User user = userDetails.getUser();

        // Sinh JWT
        String accessToken = jwtUtils.generateAccessToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Trả về AuthResponse đầy đủ
        return authMapper.toAuthResponse(user, accessToken, refreshToken);
    }


    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new ApiException(ErrorCode.INVALID_KEY);
        }

        // Lấy email từ token
        String email = jwtUtils.extractEmail(refreshToken);

        // Lấy user entity từ DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));
        // Sinh CustomUserDetails từ entity
        List<String> permissions = rolePermissionRepository.findPermissionCodesByRole(user.getRole().toUpperCase());
        CustomUserDetails userDetails = new CustomUserDetails(user,permissions);

        // Sinh token mới
        String newAccessToken = jwtUtils.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Trả về AuthResponse đầy đủ
//        return AuthResponse.builder()
//                .accessToken(newAccessToken)
//                .refreshToken(newRefreshToken)
//                .tokenType("Bearer")
//                .userId(user.getId())
//                .username(user.getUsername())
//                .email(user.getEmail())
//                .fullName(user.getFullName())
//                .role(user.getRole())
//                .build();
        return authMapper.toAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    public void changePassword(com.group7.marketplacesystem.identity.dto.request.ChangePasswordRequest request) {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        // Verify old password
        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(10);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.INCORRECT_PASSWORD);
        }

        // Verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}
