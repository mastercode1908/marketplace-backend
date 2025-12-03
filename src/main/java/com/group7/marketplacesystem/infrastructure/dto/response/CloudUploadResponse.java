package com.group7.marketplacesystem.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudUploadResponse {
    private String url;         // secure_url
    private String publicId;    // public_id
    private String mediaType;   // resource_type
    private Instant uploadedAt; // timestamp hiện tại hoặc parse created_at
}
