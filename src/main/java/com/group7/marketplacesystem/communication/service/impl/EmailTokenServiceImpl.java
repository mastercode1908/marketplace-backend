package com.group7.marketplacesystem.communication.service.impl;

import com.group7.marketplacesystem.commerce.cart.service.CartService;
import com.group7.marketplacesystem.common.security.JwtUtils;
import com.group7.marketplacesystem.communication.entity.EmailToken;
import com.group7.marketplacesystem.communication.repository.EmailTokenRepository;
import com.group7.marketplacesystem.communication.service.EmailTokenService;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.infrastructure.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailTokenServiceImpl implements EmailTokenService {

    private final EmailTokenRepository emailTokenRepository;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService; // để load UserDetails sinh JWT

    // 1. Tạo token + gửi mail xác thực
    @Override
    public void createAndSendVerificationToken(Integer userId, String userEmail) {
        // Sinh token ngẫu nhiên
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(15, ChronoUnit.MINUTES);

        // Lưu DB
        EmailToken emailToken = new EmailToken();
        emailToken.setEntityType("USER");
        emailToken.setEntityId(userId);
        emailToken.setToken(token);
        emailToken.setType("VERIFY");
        emailToken.setExpiresAt(expiry);
        emailToken.setCreatedAt(Instant.now());
        emailTokenRepository.save(emailToken);

        // Gửi mail xác thực
        mailService.sendVerificationEmail(userEmail, token);
    }


    // 2. Xác thực token + kích hoạt tài khoản + sinh JWT
    @Override
    @Transactional
    public boolean  verifyEmailToken(String token) {
        var emailTokenOpt = emailTokenRepository.findByToken(token);
        if (emailTokenOpt.isEmpty()) return false;

        EmailToken emailToken = emailTokenOpt.get();

        // Token hết hạn
        if (emailToken.getExpiresAt().isBefore(Instant.now())) {
            emailTokenRepository.delete(emailToken);
            return false;
        }

        // Cập nhật trạng thái User
        Optional<User> userOpt = userRepository.findById(emailToken.getEntityId());
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        if(user.getRole().equalsIgnoreCase("BUYER")){
            user.setUserStatus("Active");
        }else{
            user.setUserStatus("Incomplete");
        }
        user.setEmailVerified(true);
        userRepository.save(user);

        // Xóa token bằng query trực tiếp
        emailTokenRepository.deleteByToken(token);

        return true;
    }
}

