package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import lombok.Data;

/**
 * DTO cho response lấy chi tiết đơn hàng GHN
 */
@Data
public class GHNOrderDetailResponse {
    private Integer code;
    private String message;
    private GHNOrderDetailData data;
}













