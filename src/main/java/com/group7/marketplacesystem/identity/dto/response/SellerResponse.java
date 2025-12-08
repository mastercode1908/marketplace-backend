package com.group7.marketplacesystem.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerResponse {

    private UserInfo user;
    private SellerInfo seller;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private String username;
        private String email;
        private String full_name;
        private String gender;
        private String phone;
        private String avatar;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SellerInfo {
        private String shop_name;
        private String shop_address;
        private String shop_description;
        private String taxCode;
        private Integer rating_count;
        private Integer total_sales;
        private Double rating;
        private Integer positiveRatingPercentage;
        private String joinDate;
    }
}

