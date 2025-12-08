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
    private final com.group7.marketplacesystem.catalog.repository.ReviewRepository reviewRepository;

    @Override
    public SellerResponse getSellerProfile(Integer id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));
        SellerResponse response = sellerMapper.toSellerResponse(seller);
        enrichSellerResponse(response, seller);
        return response;
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
        SellerResponse response = sellerMapper.toSellerResponse(seller);
        enrichSellerResponse(response, seller);
        return response;
    }

    private void enrichSellerResponse(SellerResponse response, Seller seller) {
        if (response.getSeller() == null) return;

        // Calculate rating
        Double avgRating = reviewRepository.getSellerAverageRating(seller.getId());
        response.getSeller().setRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 5.0);

        // Calculate positive rating percentage
        Integer totalReviews = reviewRepository.countBySellerId(seller.getId());
        if (totalReviews != null && totalReviews > 0) {
            long positiveReviews = reviewRepository.countBySellerIdAndRating(seller.getId(), 5) +
                                   reviewRepository.countBySellerIdAndRating(seller.getId(), 4);
            int percentage = (int) ((positiveReviews * 100) / totalReviews);
            response.getSeller().setPositiveRatingPercentage(percentage);
        } else {
            response.getSeller().setPositiveRatingPercentage(100); // Default to 100% if no reviews
        }

        // Set join date
        if (seller.getUsers().getCreatedAt() != null) {
            response.getSeller().setJoinDate(seller.getUsers().getCreatedAt().toString());
        }
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
