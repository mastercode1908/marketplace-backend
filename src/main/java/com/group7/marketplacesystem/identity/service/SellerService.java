package com.group7.marketplacesystem.identity.service;

import com.group7.marketplacesystem.identity.dto.request.SellerRequest;
import com.group7.marketplacesystem.identity.dto.response.SellerResponse;

public interface SellerService {
    SellerResponse getSellerProfile(Integer id);
    SellerResponse updateProfileSeller(Integer id, SellerRequest request);
}
