package com.group7.marketplacesystem.infrastructure.mapper;

import com.group7.marketplacesystem.infrastructure.dto.response.CloudUploadResponse;

import java.time.Instant;
import java.util.Map;

public class CloudinaryMapper {
    public static CloudUploadResponse toResponse(Map uploadResult) {
        return CloudUploadResponse.builder()
                .url(uploadResult.get("secure_url").toString())
                .publicId(uploadResult.get("public_id").toString())
                .mediaType(uploadResult.get("resource_type").toString()) // image / video
                .uploadedAt(Instant.now())
                .build();
    }
}
