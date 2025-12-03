package com.group7.marketplacesystem.commerce.order.dto.request;

import lombok.Data;

/**
 * DTO cho request hủy đơn hàng
 */
@Data
public class CancelOrderRequest {
    /**
     * Lý do hủy đơn (bắt buộc khi seller hủy, optional khi buyer hủy)
     */
    private String reason;
}





