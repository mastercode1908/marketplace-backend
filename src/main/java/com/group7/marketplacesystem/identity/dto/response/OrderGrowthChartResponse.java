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
public class OrderGrowthChartResponse {
    private List<String> labels; // Ví dụ: ["2025-10-01", "2025-10-02", ...]
    private List<Long> orderCounts; // Số đơn theo từng ngày/tháng
    private List<Double> revenues;  // Doanh thu tương ứng theo ngày/tháng
}
