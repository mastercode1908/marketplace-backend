package com.group7.marketplacesystem.identity.mapper;

import com.group7.marketplacesystem.identity.dto.response.GoogleUserInfoResponse;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.entity.UserProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LocalUserMapper {

    public UserProvider toUserProvider(User user) {
        UserProvider up = new UserProvider();
        up.setUser(user);
        up.setProvider("LOCAL");
        up.setProviderId(null);
        up.setLinkedAt(Instant.now());
        return up;
    }
}
