package com.group7.marketplacesystem.promotion.scheduler;

import com.group7.marketplacesystem.promotion.service.ServicePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SellerPackageScheduler {

    private final ServicePackageService servicePackageService;

    // Chạy lúc 00:00 mỗi ngày
    @Scheduled(cron = "0 0 0 * * ?")
    public void runDailyExpiryCheck() {
        servicePackageService.updateExpiredPackages();
        System.out.println("Checked and updated expired packages at " + Instant.now());
    }
}

