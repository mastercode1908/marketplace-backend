package com.group7.marketplacesystem.communication.controller;

import com.group7.marketplacesystem.common.constants.MessageConstants;
import com.group7.marketplacesystem.common.response.ApiResponse;
import com.group7.marketplacesystem.communication.dto.request.ResetPasswordRequest;
import com.group7.marketplacesystem.communication.dto.request.ResetPasswordTokenRequest;
import com.group7.marketplacesystem.communication.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // Yêu cầu gửi email reset password
    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.createAndSendResetPasswordToken(request.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.success(MessageConstants.SEND_EMAIL_SUCCESS_RESETPASSWORD, null)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.fail(MessageConstants.SEND_EMAIL_FAILED + e.getMessage())
            );
        }
    }

    // Submit mật khẩu mới kèm token
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordTokenRequest request) {
        boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        if (success) {
            return ResponseEntity.ok(
                    ApiResponse.success(MessageConstants.UPDATE_PASSWORD_SUCCESS, null)
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponse.fail(MessageConstants.TOKEN_FAILED)
            );
        }
    }
}
