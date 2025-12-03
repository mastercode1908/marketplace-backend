package com.group7.marketplacesystem.promotion.scheduler;


import com.group7.marketplacesystem.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionScheduler {
    private final PromotionService promotionService;

    @Scheduled(cron = "0 0 0 * * ?") // chạy mỗi 00:00 (thứ tự giây, phút, giờ, ngày, tháng, thứ trong tuần)
                                        // @Scheduled là annotation của Spring Boot giúp bạn định nghĩa các tác vụ chạy tự động theo lịch.
    public void autoExpirePromotions() {
        promotionService.expirePromotions();
    }
}
