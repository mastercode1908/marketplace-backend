package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChartResponse {
    private List<OrderStatusData> statusData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusData {
        private String status;
        private Long count;
    }
}






