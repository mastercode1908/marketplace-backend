package com.group7.marketplacesystem.identity.controller;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.identity.dto.response.AuthResponse;
import com.group7.marketplacesystem.identity.service.IGoogleOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final IGoogleOAuthService googleOAuthService;

    /**
     * Bước 1: Gọi để chuyển hướng đến Google OAuth consent screen
     * FE có thể gọi:
     * - /api/auth/google/login?role=BUYER
     * - /api/auth/google/login?role=SELLER
     */
    @GetMapping("/register")
    public void redirectToGoogle(
            @RequestParam String role,
            HttpServletResponse response
    ) throws IOException {
        String url = googleOAuthService.buildGoogleAuthUrl(role);
        response.sendRedirect(url);
    }

    @GetMapping("/login")
    public void redirectToGoogleLogin(HttpServletResponse response) throws IOException {
        String url = googleOAuthService.buildGoogleAuthUrlLogin(); // state = "LOGIN"
        response.sendRedirect(url);
    }

    /**
     * Bước 2: Google redirect về đây sau khi user chấp nhận
     * Google sẽ trả về code và state (ở đây state = role)
     */
    // Bước 2: Google redirect về đây
    @GetMapping("/callback")
    public void handleGoogleCallback(@RequestParam("code") String code,
                                     @RequestParam(value = "state", required = false) String role,
                                     HttpServletResponse response) throws IOException {

        String frontendBaseUrl = "http://localhost:5173/google-callback"; // FE page
        try {
            AuthResponse auth = googleOAuthService.handleGoogleCallback(code, role);

            // Tạo cookie refresh token
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", auth.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // true nếu dùng HTTPS
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(Duration.ofDays(7))
                    .build();

            response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            // redirect với trạng thái thành công
            String redirectUrl = frontendBaseUrl + "?success=true";
            response.sendRedirect(redirectUrl);
            response.sendRedirect(frontendBaseUrl);

        } catch (ApiException ex) {
            // redirect với trạng thái lỗi + message
            String redirectUrl = frontendBaseUrl
                    + "?success=false"
                    + "&error=" + ex.getErrorCode().getCode();
            response.sendRedirect(redirectUrl);
        } catch (Exception ex) {
            String redirectUrl = frontendBaseUrl
                    + "?success=false"
                    + "&error=" + URLEncoder.encode("Có lỗi xảy ra khi đăng nhập Google.", StandardCharsets.UTF_8);
            response.sendRedirect(redirectUrl);
        }
    }
}
