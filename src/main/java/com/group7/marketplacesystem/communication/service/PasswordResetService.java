package com.group7.marketplacesystem.communication.service;

import java.util.Optional;

public interface PasswordResetService {
    // Tạo token reset password và gửi mail
    void createAndSendResetPasswordToken(String userEmail);

    // Xác thực token và đổi mật khẩu mới
    boolean resetPassword(String token, String newPassword);

    // Tùy chọn: lấy userId từ token (nếu cần)
    Optional<Integer> getUserIdFromToken(String token);
}
