package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import lombok.Data;

/**
 * DTO cho response lấy thông tin shop từ GHN API
 * Endpoint /v2/shop/all trả về {last_offset, shops: [...]}
 */
@Data
public class GHNShopInfoResponse {
    private Integer code;
    private String message;
    // Có thể là Map {last_offset, shops: [...]} hoặc object đơn - dùng Object để handle cả 2 trường hợp
    private Object data;
}

