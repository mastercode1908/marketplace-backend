package com.group7.marketplacesystem.common.security;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {

    private static CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED);
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails;
        }

        throw new ApiException(ErrorCode.UNAUTHENTICATED);
    }

    public static Integer getUserId() {
        return getCurrentUser().getId();
    }

    public static String getUserRole() {
        return getCurrentUser().getRole();
    }
}
