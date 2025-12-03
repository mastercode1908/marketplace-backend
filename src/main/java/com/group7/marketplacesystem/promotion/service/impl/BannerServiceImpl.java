package com.group7.marketplacesystem.promotion.service.impl;



import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.infrastructure.service.MailService;
import com.group7.marketplacesystem.promotion.dto.request.CreateBannerRequest;
import com.group7.marketplacesystem.promotion.dto.response.BannerResponse;
import com.group7.marketplacesystem.promotion.dto.response.HomepageBannersResponse;
import com.group7.marketplacesystem.promotion.entity.Banner;
import com.group7.marketplacesystem.promotion.entity.Banner.BannerStatus;
import com.group7.marketplacesystem.promotion.mapper.BannerMapper;
import com.group7.marketplacesystem.promotion.repository.BannerRepository;
import com.group7.marketplacesystem.promotion.repository.PackageUsageRepository;
import com.group7.marketplacesystem.promotion.repository.SellerPackageRepository;
import com.group7.marketplacesystem.promotion.service.BannerService;
import com.group7.marketplacesystem.promotion.entity.Sellerpackage;
import com.group7.marketplacesystem.promotion.entity.PackageUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final SellerRepository sellerRepository;

    private final UserRepository userRepository;
    private final MailService mailService;
    private final BannerMapper bannerMapper;
    private final SellerPackageRepository sellerPackageRepository;
    private final PackageUsageRepository packageUsageRepository;

    @Override
    @Transactional
    public BannerResponse createBanner(Integer sellerId, CreateBannerRequest request) {

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        // Check ngày hợp lệ
        Instant now = Instant.now();
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException(ErrorCode.INVALID_DATE_RANGE);
        }
        if (request.getStartDate().isBefore(now.minusSeconds(86400))) { // không cho quá khứ
            throw new ApiException(ErrorCode.INVALID_START_DATE);
        }

        Instant maxEndDate = request.getStartDate().plusSeconds(365L * 24 * 60 * 60); // max 1 năm
        if (request.getEndDate().isAfter(maxEndDate)) {
            throw new ApiException(ErrorCode.INVALID_END_DATE);
        }

        // Check for active package and deduct usage
        List<Sellerpackage> packages = sellerPackageRepository.findBySellerId(sellerId);
        Sellerpackage activePackage = packages.stream()
                .filter(p -> "Active".equals(p.getStatus()) && p.getRemainingUsage() > 0 && p.getEndDate().isAfter(Instant.now()))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST)); // Using BAD_REQUEST for now as NO_ACTIVE_PACKAGE might not exist

        if (request.getEndDate().isAfter(activePackage.getEndDate())) {
            throw new ApiException(ErrorCode.INVALID_BANNER_END_DATE);
        }
        // Deduct usage
        activePackage.setRemainingUsage(activePackage.getRemainingUsage() - 1);
        sellerPackageRepository.save(activePackage);

        // Tạo entity Banner
        Banner banner = bannerMapper.toEntity(request, seller);

        // Lưu vào DB
        banner = bannerRepository.save(banner);

        // Record usage
        PackageUsage usage = new PackageUsage();
        usage.setSellerPackage(activePackage);
        usage.setTargetId(banner.getId());
        usage.setTargetType("BANNER");
        usage.setUsedAt(Instant.now());
        packageUsageRepository.save(usage);

        return bannerMapper.toResponse(banner);

    }

    @Override
    public BannerResponse getBannerById(Integer bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BANNER_NOT_FOUND));
        return bannerMapper.toResponse(banner);
    }

    @Override
    public Page<BannerResponse> getSellerBanners(Integer sellerId, Pageable pageable) {
        Page<Banner> banners = bannerRepository.findBySeller_IdOrderByCreatedAtDesc(sellerId, pageable);
        List<BannerResponse> responses = banners.getContent().stream()
                .map(bannerMapper::toResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, banners.getTotalElements());
    }

    @Override
    @Transactional
    public BannerResponse updateBanner(Integer sellerId, Integer bannerId, CreateBannerRequest request) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BANNER_NOT_FOUND));

        if (!banner.getSeller().getId().equals(sellerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if ((banner.getStatus() != BannerStatus.PENDING && banner.getStatus()!=BannerStatus.PAUSED )
                && banner.getStatus() != BannerStatus.REJECTED) {
            throw new ApiException(ErrorCode.INVALID_BANNER_STATUS);
        }

        banner.setTitle(request.getTitle());
        banner.setDescription(request.getDescription());
        banner.setImageUrl(request.getImageUrl());
        banner.setStartDate(request.getStartDate());
        banner.setEndDate(request.getEndDate());
        banner.setStatus(BannerStatus.PENDING);

        banner = bannerRepository.save(banner);
        return bannerMapper.toResponse(banner);
    }

    @Override
    @Transactional
    public void deleteBanner(Integer sellerId, Integer bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BANNER_NOT_FOUND));

        if (!banner.getSeller().getId().equals(sellerId)) {
            throw new ApiException(ErrorCode.SELLER_NOT_FOUND);
        }

        banner.setDeletedAt(Instant.now());
        bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public BannerResponse approveBanner(Integer bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BANNER_NOT_FOUND));

        if (banner.getStatus() != BannerStatus.PENDING) {
            throw new ApiException(ErrorCode.INVALID_BANNER_STATUS);
        }

        banner.setStatus(BannerStatus.ACTIVE);
        banner.setRejectionReason(null);
        banner = bannerRepository.save(banner);
        User user = userRepository.findById(banner.getSeller().getId())
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));
        mailService.sendBannerApprovedEmail(user.getEmail(), banner.getTitle());
        return bannerMapper.toResponse(banner);
    }

    @Override
    @Transactional
    public BannerResponse rejectBanner(Integer bannerId, String rejectionReason) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BANNER_NOT_FOUND));

        if (banner.getStatus() != BannerStatus.PENDING) {
            throw new ApiException(ErrorCode.INVALID_BANNER_STATUS);
        }

        banner.setStatus(BannerStatus.REJECTED);
        banner.setRejectionReason(rejectionReason);
        banner = bannerRepository.save(banner);

        User user = userRepository.findById(banner.getSeller().getId())
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        mailService.sendBannerRejectionEmail(user.getEmail(), banner, rejectionReason);

        return bannerMapper.toResponse(banner);
    }

    @Override
    @Transactional
    public BannerResponse pauseBanner(Integer sellerId, Integer bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BANNER_NOT_FOUND));

        if (!banner.getSeller().getId().equals(sellerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (banner.getStatus() != BannerStatus.ACTIVE) {
            throw new ApiException(ErrorCode.INVALID_BANNER_STATUS);
        }

        banner.setStatus(BannerStatus.PAUSED);
        banner = bannerRepository.save(banner);
        return bannerMapper.toResponse(banner);
    }

    @Override
    @Transactional
    public BannerResponse resumeBanner(Integer sellerId, Integer bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BANNER_NOT_FOUND));

        if (!banner.getSeller().getId().equals(sellerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (banner.getStatus() != BannerStatus.PAUSED) {
            throw new ApiException(ErrorCode.INVALID_BANNER_STATUS);
        }

        banner.setStatus(BannerStatus.ACTIVE);
        banner = bannerRepository.save(banner);
        return bannerMapper.toResponse(banner);
    }

    @Override
    public Page<BannerResponse> getAllBanners(Pageable pageable) {
        Page<Banner> banners = bannerRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<BannerResponse> responses = banners.getContent().stream()
                .map(bannerMapper::toResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, banners.getTotalElements());
    }

    @Override
    public HomepageBannersResponse getHomepageBanners() {
        Instant now = Instant.now();
        List<Banner> activeBanners = bannerRepository.findActiveBanners(BannerStatus.ACTIVE);
        List<BannerResponse> responses = activeBanners.stream()
                .map(bannerMapper::toResponse)
                .collect(Collectors.toList());
        return new HomepageBannersResponse(responses);
    }
}
