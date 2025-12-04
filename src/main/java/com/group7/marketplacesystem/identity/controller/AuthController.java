package com.group7.marketplacesystem.identity.controller;


import com.group7.marketplacesystem.common.constants.MessageConstants;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.response.ApiResponse;
import com.group7.marketplacesystem.identity.dto.request.AuthRequest;
import com.group7.marketplacesystem.identity.dto.request.RefreshTokenRequest;
import com.group7.marketplacesystem.identity.dto.response.AuthResponse;
import com.group7.marketplacesystem.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    //    ? là wildcard trong Java generics, cho phép body có thể là AuthResponse, Map, String, v.v.
//    Ở method login(), tuỳ trường hợp:
//    Nếu login thành công → body là AuthResponse.
//    Nếu thất bại → body là Map<String, String> chứa thông báo lỗi.
//    neu du an that .sameSite("Strict"), secure(true)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        // Đóng gói refresh token vào cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(true) // false khi chạy local, true khi deploy HTTPS
                .path("/")
                .sameSite("None") // local nên để Lax để FE gửi được cookie
                .maxAge(Duration.ofDays(7))
                .build();

        // Không gửi refresh token trong body (chỉ để trong cookie)
        response.setRefreshToken(null);

        // Trả response có cookie
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success(MessageConstants.LOGIN_SUCCESS, response));

    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String cookieRefreshToken,
            @RequestBody(required = false) RefreshTokenRequest bodyRequest) {

        // Ưu tiên cookie → fallback body
        String refreshToken = (cookieRefreshToken != null)
                ? cookieRefreshToken
                : (bodyRequest != null ? bodyRequest.getRefreshToken() : null);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ApiException(ErrorCode.INVALID_KEY);
        }

        // Gọi service để tạo token mới
        AuthResponse response = authService.refreshToken(refreshToken);

        // Gắn refreshToken mới vào cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(true) // false nếu đang dev local
                .path("/")
                .sameSite("None") // local nên để Lax để FE gửi được cookie
                .maxAge(Duration.ofDays(7))
                .build();

        // Không trả refreshToken trong body nữa
        response.setRefreshToken(null);

        // Trả về accessToken + user info
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(MessageConstants.REFRESH_TOKEN, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // Tạo cookie "xoá" refresh token
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true) // false nếu dev local
                .sameSite("None")
                .path("/")
                .maxAge(0) // 0 = xóa cookie
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(ApiResponse.success(MessageConstants.LOG_OUT,null));
    }
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody com.group7.marketplacesystem.identity.dto.request.ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.UPDATE_PASSWORD_SUCCESS, null));
    }
}
