package com.group7.marketplacesystem.identity.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuyerUpdateRequest {
    private BuyerRequest buyer;
    private UserRequest user;

}
