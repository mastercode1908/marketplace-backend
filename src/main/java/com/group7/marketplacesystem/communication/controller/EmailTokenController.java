package com.group7.marketplacesystem.communication.controller;

import com.group7.marketplacesystem.common.constants.MessageConstants;
import com.group7.marketplacesystem.common.response.ApiResponse;
import com.group7.marketplacesystem.communication.dto.request.EmailTokenRequest;
import com.group7.marketplacesystem.communication.service.EmailTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailTokenController {

    private final EmailTokenService emailTokenService;


    // 1. Gửi email xác thực (POST /api/auth/email/send)
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendVerificationEmail(@RequestBody EmailTokenRequest request) {
        try {
            emailTokenService.createAndSendVerificationToken(request.getUserId(), request.getUserEmail());
            return ResponseEntity.ok(
                    ApiResponse.success(MessageConstants.SEND_EMAIL_SUCCESS + request.getUserEmail(), null)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.fail(MessageConstants.SEND_EMAIL_FAILED + e.getMessage())
            );
        }
    }

    // 2. Xác thực token (GET /api/auth/email/verify?token=...)
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        boolean success = emailTokenService.verifyEmailToken(token);
        if (!success) {
            return ResponseEntity.ok(
                    ApiResponse.fail(MessageConstants.TOKEN_FAILED)
            );
        }
        return ResponseEntity.ok(
                ApiResponse.success(MessageConstants.TOKEN_SUCCESS, null)
        );
    }
}
