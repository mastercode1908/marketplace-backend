package com.group7.marketplacesystem.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductMediaResponse {
    private Integer mediaId;
    private String url;
    private String publicId;
    private String mediaType; // "IMAGE" or "VIDEO"
    private Integer position;
    private Instant createdAt;
    private Instant deletedAt;

    // getters & setters

}
