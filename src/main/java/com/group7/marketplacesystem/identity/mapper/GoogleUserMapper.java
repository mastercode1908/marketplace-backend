package com.group7.marketplacesystem.identity.mapper;

import com.group7.marketplacesystem.identity.dto.response.GoogleUserInfoResponse;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.entity.UserProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class GoogleUserMapper {
    public static User toUser(GoogleUserInfoResponse info) {
        User user = new User();
        user.setEmail(info.getEmail());
        user.setFullName(info.getName());
        user.setAvatarUrl(info.getPicture());
        user.setEmailVerified(info.isEmail_verified());
        return user;
    }

    public static UserProvider toUserProvider(User user, GoogleUserInfoResponse info) {
        UserProvider up = new UserProvider();
        up.setUser(user);
        up.setProvider("GOOGLE");
        up.setProviderId(info.getSub());
        up.setLinkedAt(Instant.now());
        return up;
    }
}
