package com.group7.marketplacesystem.promotion.service;

import com.group7.marketplacesystem.promotion.dto.request.CreateBannerRequest;
import com.group7.marketplacesystem.promotion.dto.response.BannerResponse;
import com.group7.marketplacesystem.promotion.dto.response.HomepageBannersResponse;
import com.group7.marketplacesystem.promotion.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BannerService {

    // CRUD cơ bản
    BannerResponse createBanner(Integer sellerId, CreateBannerRequest request);

    BannerResponse getBannerById(Integer bannerId);

    Page<BannerResponse> getSellerBanners(Integer sellerId, Pageable pageable);

    BannerResponse updateBanner(Integer sellerId, Integer bannerId, CreateBannerRequest request);

    void deleteBanner(Integer sellerId, Integer bannerId);

    // Quản lý trạng thái banner
    BannerResponse approveBanner(Integer bannerId);

    BannerResponse rejectBanner(Integer bannerId, String rejectionReason);

    BannerResponse pauseBanner(Integer sellerId, Integer bannerId);

    BannerResponse resumeBanner(Integer sellerId, Integer bannerId);

    // Lấy banner theo trạng thái linh hoạt
    Page<BannerResponse> getAllBanners(Pageable pageable);

    HomepageBannersResponse getHomepageBanners();


}
