package com.group7.marketplacesystem.identity.dto.response;

import com.group7.marketplacesystem.identity.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopInfoResponse {

    private Integer sellerId;
    private String shopName;
    private String shopAddress;
    private String shopDescription;
    private String taxCode;
    private UserToAdminResponse user;

}
