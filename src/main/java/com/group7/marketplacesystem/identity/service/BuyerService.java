package com.group7.marketplacesystem.identity.service;

import com.group7.marketplacesystem.identity.dto.request.BuyerUpdateRequest;
import com.group7.marketplacesystem.identity.dto.response.BuyerResponse;
import com.group7.marketplacesystem.identity.entity.Buyer;

import java.util.List;

public interface BuyerService {

    BuyerResponse getBuyerById(Integer buyerId);

    List<BuyerResponse> getAllBuyer();

    BuyerResponse updateBuyer(BuyerUpdateRequest buyerUpdateRequest);

    Buyer getBuyerByBuyerId(Integer buyerId);

    BuyerResponse getBuyer();
}
