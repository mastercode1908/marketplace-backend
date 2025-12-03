package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCommissionResponse {
    private Integer orderId;              // ID đơn hàng
    private LocalDateTime orderDate;      // Ngày tạo đơn
    private double orderTotal;            // Tổng tiền đơn hàng
    private double commissionRate;        // Tỷ lệ hoa hồng (7.0%)
    private double commissionAmount;      // Số tiền hoa hồng (orderTotal * 0.07)
    private String status;                // Trạng thái đơn hàng
}
