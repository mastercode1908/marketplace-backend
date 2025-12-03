package com.group7.marketplacesystem.commerce.shipping.service;

import com.group7.marketplacesystem.commerce.shipping.dto.ghn.*;
import com.group7.marketplacesystem.commerce.shipping.dto.request.BuyerAddressRequest;
import com.group7.marketplacesystem.commerce.shipping.dto.request.GHNShopInfoRequest;
import com.group7.marketplacesystem.commerce.shipping.dto.response.BuyerAddressResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.CheckoutResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.GHNShopInfoPreviewResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.GHNShopInfoResponse;

import java.util.List;

public interface ShippingService {
    List<GHNProvince> getProvinces();
    List<GHNDistrict> getDistricts(Integer provinceId);
    List<GHNWard> getWards(Integer districtId);
    BuyerAddressResponse createAddress(Integer buyerId, BuyerAddressRequest request);
    List<BuyerAddressResponse> getAddressesByBuyerId(Integer buyerId);
    BuyerAddressResponse getAddressById(Integer addressId, Integer buyerId);
    /**
     * Lấy địa chỉ theo ID kể cả khi đã bị xóa (soft delete)
     * Dùng cho mục đích xem lịch sử đơn hàng - cần hiển thị địa chỉ gốc của đơn hàng
     */
    BuyerAddressResponse getAddressByIdForOrder(Integer addressId, Integer buyerId);
    void deleteAddress(Integer buyerId, Integer addressId);
    CheckoutResponse prepareCheckout(Integer buyerId, Integer addressId);
    /**
     * Seller nhập/cập nhật thông tin GHN shop
     * Tự động lấy thông tin shop từ GHN API và lưu vào database
     */
    GHNShopInfoResponse saveOrUpdateGHNShopInfo(Integer sellerId, GHNShopInfoRequest request);
    /**
     * Lấy thông tin GHN shop của seller
     */
    GHNShopInfoResponse getGHNShopInfo(Integer sellerId);
    /**
     * Preview shop info từ GHN (không lưu vào DB)
     * Dùng để lấy thông tin shop từ GHN và tự động điền vào form
     */
    GHNShopInfoPreviewResponse previewGHNShopInfo(String token, Integer shopCode);
    
    /**
     * Tính phí vận chuyển cho một sản phẩm cụ thể
     */
    java.math.BigDecimal calculateFee(com.group7.marketplacesystem.commerce.shipping.dto.request.CalculateFeeRequest request);
}


