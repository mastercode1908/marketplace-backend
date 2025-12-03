package com.group7.marketplacesystem.identity.service.impl;

import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.identity.dto.request.SellerRequest;
import com.group7.marketplacesystem.identity.dto.response.SellerResponse;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.mapper.SellerMapper;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.identity.service.SellerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final SellerMapper sellerMapper;

    @Override
    public SellerResponse getSellerProfile(Integer id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));
        return sellerMapper.toSellerResponse(seller);
    }

    @Override
    public SellerResponse updateProfileSeller(Integer id, SellerRequest request) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));


        seller.getUsers().setFullName(validateFullName(request.getFull_name(), ErrorCode.INVALID_FULLNAME));
        seller.getUsers().setGender(request.getGender());
        seller.getUsers().setPhone(validatePhone(request.getPhone(), ErrorCode.INVALID_PHONE_NUMBER));
        seller.getUsers().setAvatarUrl(request.getAvatar());

        seller.setShopName(validateAddressField(request.getShop_name(), ErrorCode.INVALID_SHOP_NAME));
        seller.setShopAddress(validateAddressField(request.getShop_address(), ErrorCode.INVALID_ADDRESS));
        seller.setShopDescription(validateAddressField(request.getShop_description(), ErrorCode.INVALID_SHOP_DESCRIPTION));
        sellerRepository.save(seller);
        return sellerMapper.toSellerResponse(seller);
    }

    public String validateFullName(String name, ErrorCode error) {
        if (name == null) {
            throw new ApiException(error);
        }
        // Kiểm tra space đầu
        if (name.startsWith(" ")) {
            throw new ApiException(error);
        }
        String cleaned = name.trim();
        if (cleaned.isEmpty()) {
            throw new ApiException(error);
        }
        if (cleaned.matches(".*\\s{2,}.*")) {
            throw new ApiException(error); // quá 1 space liên tiếp
        }
        if (!cleaned.matches("^[\\p{L}\\s]+$")) { // chỉ chữ + space
            throw new ApiException(error);
        }
        return cleaned;
    }

    public String validatePhone(String phone, ErrorCode error) {
        if (phone == null || phone.isEmpty()) {
            throw new ApiException(error); // không được để trống
        }
        // Xóa tất cả khoảng trắng, kể cả đầu/cuối
        String cleaned = phone.replaceAll("\\s+", "");

        // Kiểm tra đúng 10 số
        if (!cleaned.matches("^\\d{10}$")) {
            throw new ApiException(error);
        }

        return cleaned;
    }

    public String validateAddressField(String value, ErrorCode error) {
        if (value == null) return null;

        if (value.startsWith(" ")) {
            throw new ApiException(error);
        }

        String cleaned = value.trim();
        if (cleaned.isEmpty()) {
            throw new ApiException(error);
        }

        if (cleaned.matches(".*\\s{2,}.*")) {
            throw new ApiException(error);
        }

        // Cho phép nhiều ký tự dùng trong địa chỉ
        if (!cleaned.matches("^[\\p{L}\\p{N}\\s,\\.!:/#()\\-]+$")) {
            throw new ApiException(error);
        }

        return cleaned;
    }

}
