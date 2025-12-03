package com.group7.marketplacesystem.identity.validator;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;

public class PasswordValidator {

    public static void validate(String password) {
        if (!password.matches(".*[A-Z].*")) {
            throw new ApiException(ErrorCode.PASSWORD_NO_UPPERCASE);
        }
        if (!password.matches(".*[a-z].*")) {
            throw new ApiException(ErrorCode.PASSWORD_NO_LOWERCASE);
        }
        if (!password.matches(".*\\d.*")) {
            throw new ApiException(ErrorCode.PASSWORD_NO_NUMBER);
        }
        if (!password.matches(".*[@$!%*?&].*")) {
            throw new ApiException(ErrorCode.PASSWORD_NO_SPECIAL);
        }
    }
}
