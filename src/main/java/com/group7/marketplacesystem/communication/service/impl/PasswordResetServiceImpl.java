package com.group7.marketplacesystem.communication.service.impl;

import com.group7.marketplacesystem.communication.entity.EmailToken;
import com.group7.marketplacesystem.communication.repository.EmailTokenRepository;
import com.group7.marketplacesystem.communication.service.PasswordResetService;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.entity.UserProvider;
import com.group7.marketplacesystem.identity.mapper.LocalUserMapper;
import com.group7.marketplacesystem.identity.repository.UserProviderRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.identity.validator.PasswordValidator;
import com.group7.marketplacesystem.infrastructure.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final EmailTokenRepository emailTokenRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final LocalUserMapper localUserMapper;
    private final UserProviderRepository userProviderRepository;

    @Override
    @Async // <-- Bất đồng bộ
    public void createAndSendResetPasswordToken(String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) return;
        if(userOpt.get().getUserStatus().equalsIgnoreCase("Banned")) return;
        if(userOpt.get().getUserStatus().equalsIgnoreCase("Inactive")) return;
        if(userOpt.get().getDeletedAt() != null) return;

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(15, ChronoUnit.MINUTES);

        EmailToken emailToken = new EmailToken();
        emailToken.setEntityType("USER");
        emailToken.setEntityId(user.getId());
        emailToken.setToken(token);
        emailToken.setType("RESET");
        emailToken.setExpiresAt(expiry);
        emailToken.setCreatedAt(Instant.now());
        emailTokenRepository.save(emailToken);

        mailService.sendResetPasswordEmail(userEmail, token);
    }

    @Override
    public boolean resetPassword(String token, String newPassword) {
        Optional<EmailToken> emailTokenOpt = emailTokenRepository.findByToken(token);
        if (emailTokenOpt.isEmpty()) return false;

        EmailToken emailToken = emailTokenOpt.get();
        if (emailToken.getExpiresAt().isBefore(Instant.now())) {
            emailTokenRepository.delete(emailToken);
            return false;
        }

        Optional<User> userOpt = userRepository.findById(emailToken.getEntityId());
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        // Kiểm tra user có password hay chưa (Google login sẽ không có)

        // Validate password nâng cao ở service
        PasswordValidator.validate(newPassword);

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            UserProvider userProvider = localUserMapper.toUserProvider(user);
            userProviderRepository.save(userProvider);
            // Tạo local password cho user Google
            user.setPassword(passwordEncoder.encode(newPassword));
//            user.setPassword(newPassword);
        } else {
            // User bình thường, update password
            user.setPassword(passwordEncoder.encode(newPassword));
//            user.setPassword(newPassword);
        }
        userRepository.save(user);

        emailTokenRepository.delete(emailToken);
        return true;
    }

    @Override
    public Optional<Integer> getUserIdFromToken(String token) {
        Optional<EmailToken> emailTokenOpt = emailTokenRepository.findByToken(token);
        return emailTokenOpt.map(EmailToken::getEntityId);
    }
}
