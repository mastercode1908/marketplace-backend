package com.group7.marketplacesystem.communication.service;

import java.util.Map;
import java.util.Optional;

public interface  EmailTokenService {
    void createAndSendVerificationToken(Integer userId, String userEmail);
    boolean verifyEmailToken(String token);
}
