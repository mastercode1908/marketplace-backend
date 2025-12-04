package com.group7.marketplacesystem.promotion.mapper;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.promotion.dto.request.CreateBannerRequest;
import com.group7.marketplacesystem.promotion.dto.response.BannerResponse;
import com.group7.marketplacesystem.promotion.entity.Banner;
import com.group7.marketplacesystem.promotion.entity.Sellerpackage;
import org.springframework.stereotype.Component;
@Component
public class BannerMapper {
    public Banner toEntity(CreateBannerRequest request, Seller seller) {
        Banner banner = new Banner();
        banner.setSeller(seller);
        banner.setTitle(request.getTitle());
        banner.setDescription(request.getDescription());
        banner.setImageUrl(request.getImageUrl());
        banner.setStartDate(request.getStartDate());
        banner.setEndDate(request.getEndDate());
        banner.setStatus(Banner.BannerStatus.PENDING);
        banner.setPosition(0);
        banner.setPriority(0);
        banner.setDeletedAt(null); // mới thêm, mặc định chưa xóa
        return banner;
    }
    public BannerResponse toResponse(Banner banner) {
        return BannerResponse.builder()
                .bannerId(banner.getId())
                .shopName(banner.getSeller().getShopName())
                .sellerId(banner.getSeller().getId())
                .title(banner.getTitle())
                .description(banner.getDescription())
                .imageUrl(banner.getImageUrl())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .status(banner.getStatus())
                .rejectionReason(banner.getRejectionReason())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }
}