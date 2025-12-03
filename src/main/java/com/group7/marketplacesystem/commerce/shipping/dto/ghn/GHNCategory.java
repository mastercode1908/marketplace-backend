package com.group7.marketplacesystem.commerce.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho category của item GHN
 */
@Data
public class GHNCategory {
    @JsonProperty("level1")
    private String level1;
}













