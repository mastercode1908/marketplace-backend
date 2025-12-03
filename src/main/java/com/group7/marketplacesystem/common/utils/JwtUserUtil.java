package com.group7.marketplacesystem.common.utils;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.security.JwtUtils;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JwtUserUtil {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public Integer extractUserId(String token) {
        String email = jwtUtils.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));
        return user.getId();
    }
}
