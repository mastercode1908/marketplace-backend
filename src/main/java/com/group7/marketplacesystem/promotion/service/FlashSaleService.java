package com.group7.marketplacesystem.promotion.service;

import com.group7.marketplacesystem.promotion.dto.request.FlashSaleRequest;
import com.group7.marketplacesystem.promotion.dto.response.FlashSaleResponse;

import java.util.List;

public interface FlashSaleService {
    List<FlashSaleResponse>getAllFlashSale();
    FlashSaleResponse createFlashSale(FlashSaleRequest request, Integer adminId);
    FlashSaleResponse updateFlashSale(Integer id, FlashSaleRequest request);
    FlashSaleResponse getFlashSaleById(Integer id);
    void deleteFlashSale(Integer id);
}
