package com.group7.marketplacesystem.catalog.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishlistRequest {
    private Integer productId;
    private String text;
}
