package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho item trong đơn hàng GHN
 */
@Data
public class GHNItem {
    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("price")
    private Integer price; // Giá tiền (VND)

    @JsonProperty("weight")
    private Integer weight; // Trọng lượng (gram)

    @JsonProperty("category")
    private GHNCategory category;
}













