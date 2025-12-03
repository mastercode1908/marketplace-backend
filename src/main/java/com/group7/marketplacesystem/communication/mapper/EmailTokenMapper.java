package com.group7.marketplacesystem.communication.mapper;

import com.group7.marketplacesystem.communication.entity.EmailToken;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class EmailTokenMapper {

    // Khi tạo EmailToken từ Request (nếu cần)
    public static EmailToken toEntity(Integer userId, String entityType, String type, String token, Instant expiresAt) {
        EmailToken entity = new EmailToken();
        entity.setEntityId(userId);
        entity.setEntityType(entityType != null ? entityType : "USER");
        entity.setType(type != null ? type : "VERIFY");
        entity.setToken(token);
        entity.setExpiresAt(expiresAt != null ? expiresAt : Instant.now().plusSeconds(900)); // default 15 phút
        entity.setCreatedAt(Instant.now());
        return entity;
    }

}
