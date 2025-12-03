package com.group7.marketplacesystem.infrastructure.mapper;

import com.group7.marketplacesystem.infrastructure.dto.response.SystemlogResponse;
import com.group7.marketplacesystem.infrastructure.entity.Systemlog;
import org.springframework.stereotype.Component;

@Component
public class SystemlogMapper {

    public SystemlogResponse toResponse(Systemlog systemlog) {
        if (systemlog == null) {
            return null;
        }
        SystemlogResponse response = new SystemlogResponse();
        response.setId(systemlog.getId());
        response.setUserId(systemlog.getUser().getId());
        response.setAction(systemlog.getAction());
        response.setDescription(systemlog.getDescription());
        response.setCreatedAt(systemlog.getCreatedAt());
        response.setIpAddress(systemlog.getIpAddress());
        return response;
    }
}
