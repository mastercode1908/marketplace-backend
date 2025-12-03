package com.group7.marketplacesystem.promotion.controller;

import com.group7.marketplacesystem.promotion.dto.response.HomepageBannersResponse;
import com.group7.marketplacesystem.promotion.service.BannerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;
    @GetMapping("/homepage")
    public ResponseEntity<HomepageBannersResponse> getHomepageBanners() {
        HomepageBannersResponse response = bannerService.getHomepageBanners();
        return ResponseEntity.ok(response);
    }

}
