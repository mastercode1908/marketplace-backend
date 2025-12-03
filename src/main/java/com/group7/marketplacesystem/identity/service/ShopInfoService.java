package com.group7.marketplacesystem.identity.service;

import com.group7.marketplacesystem.identity.dto.request.ShopInfoRequest;
import com.group7.marketplacesystem.identity.dto.response.ShopInfoResponse;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.entity.User;

public interface ShopInfoService {
    void postShopInfo(ShopInfoRequest request);
}
