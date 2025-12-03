package com.group7.marketplacesystem.promotion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleResponse {
    private Integer id;
    private String name;
    private LocalDateTime start_date;
    private LocalDateTime end_date;
}
