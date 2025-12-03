package com.group7.marketplacesystem.promotion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomepageBannersResponse {
    private List<BannerResponse> banners;
}
