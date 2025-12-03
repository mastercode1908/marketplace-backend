package com.group7.marketplacesystem.commerce.shipping.mapper;

import com.group7.marketplacesystem.commerce.cart.entity.Cartitem;
import com.group7.marketplacesystem.commerce.shipping.dto.response.BuyerAddressResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.CartItemResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.GHNShopInfoResponse;
import com.group7.marketplacesystem.commerce.shipping.entity.GHNShopInfo;
import com.group7.marketplacesystem.identity.entity.BuyerAddress;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ShippingMapper {

    /**
     * Map từ BuyerAddress entity → BuyerAddressResponse
     */
    public BuyerAddressResponse toResponse(BuyerAddress address) {
        if (address == null) {
            return null;
        }
        BuyerAddressResponse response = new BuyerAddressResponse();
        response.setId(address.getId());
        response.setBuyerId(address.getBuyer().getId());
        response.setReceiverName(address.getReceiverName());
        response.setReceiverPhone(address.getReceiverPhone());
        response.setAddressDetail(address.getAddressDetail());
        response.setWardCode(address.getWardCode());
        response.setDistrictId(address.getDistrictId());
        response.setProvinceName(address.getProvinceName());
        response.setIsDefault(address.getIsDefault());
        response.setCreatedAt(address.getCreatedAt());
        response.setUpdatedAt(address.getUpdatedAt());
        return response;
    }

    /**
     * Map từ Cartitem entity → CartItemResponse
     */
    public CartItemResponse toCartItemResponse(Cartitem cartItem) {
        if (cartItem == null) {
            return null;
        }
        CartItemResponse response = new CartItemResponse();
        response.setItemId(cartItem.getId());
        response.setProductId(cartItem.getProduct().getId());
        response.setProductName(cartItem.getProduct().getName());
        response.setQuantity(cartItem.getQuantity());
        response.setUnitPrice(cartItem.getUnitPrice());
        response.setSubtotal(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        response.setSellerId(cartItem.getProduct().getSeller().getId());
        response.setSellerName(cartItem.getProduct().getSeller().getShopName());
        return response;
    }

    /**
     * Map từ GHNShopInfo entity → GHNShopInfoResponse
     * Note: Method này chỉ map các field cơ bản, không bao gồm logic tìm province_id từ district_id
     * Logic tìm province_id nên được xử lý riêng trong service nếu cần
     * Token không được trả về để bảo mật
     */
    public GHNShopInfoResponse toGHNShopInfoResponse(GHNShopInfo shopInfo) {
        if (shopInfo == null) {
            return null;
        }
        GHNShopInfoResponse response = new GHNShopInfoResponse();
        response.setId(shopInfo.getId());
        response.setSellerId(shopInfo.getSeller().getId());
        // Không trả về token để bảo mật
        response.setGhnToken(null);
        response.setGhnShopCode(shopInfo.getGhnShopCode());
        response.setGhnShopName(shopInfo.getGhnShopName());
        response.setPickupAddress(shopInfo.getPickupAddress());
        response.setPickupDistrictId(shopInfo.getPickupDistrictId());
        response.setPickupWardCode(shopInfo.getPickupWardCode());
        // pickupProvinceId sẽ được set riêng nếu cần (logic phức tạp trong service)
        return response;
    }
}


