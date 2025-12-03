package com.group7.marketplacesystem.promotion.mapper;

import com.group7.marketplacesystem.identity.entity.Admin;
import com.group7.marketplacesystem.promotion.dto.request.FlashSaleRequest;
import com.group7.marketplacesystem.promotion.dto.response.FlashSaleResponse;
import com.group7.marketplacesystem.promotion.entity.Flashsale;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class FlashSaleMapper {
    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");
    public Flashsale toEntity(FlashSaleRequest request, Integer adminId) {
        if (request == null) return null;

        Flashsale flashsale = new Flashsale();
        Admin admin = new Admin();
        admin.setId(adminId);
        flashsale.setAdmin(admin);
        flashsale.setName(request.getName());
        flashsale.setStartDate(request.getStart_date().atZone(ZONE_VN).toInstant());
        flashsale.setEndDate(request.getEnd_date().atZone(ZONE_VN).toInstant());
        return flashsale;
    }

    public FlashSaleResponse toResponse(Flashsale flashsale) {
        if (flashsale == null) return null;
        FlashSaleResponse flashSaleResponse = new FlashSaleResponse();
        flashSaleResponse.setId(flashsale.getId());
        flashSaleResponse.setName(flashsale.getName());
        flashSaleResponse.setStart_date(flashsale.getStartDate().atZone(ZONE_VN).toLocalDateTime());
        flashSaleResponse.setEnd_date(flashsale.getEndDate().atZone(ZONE_VN).toLocalDateTime());
        return flashSaleResponse;
    }
}
