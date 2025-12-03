package com.group7.marketplacesystem.catalog.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportMediaDTO {
    private String url;
    private String publicId;
    private String mediaType;
}
