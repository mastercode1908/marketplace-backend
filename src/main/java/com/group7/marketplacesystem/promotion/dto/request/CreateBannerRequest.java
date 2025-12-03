package com.group7.marketplacesystem.promotion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBannerRequest {



    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String imageUrl;

    private String linkUrl;

    private Integer targetId;

    @NotNull
    private Instant startDate;

    @NotNull
    private Instant endDate;
}
