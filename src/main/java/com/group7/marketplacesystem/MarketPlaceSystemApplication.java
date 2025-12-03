package com.group7.marketplacesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync // Bật hỗ trợ @Async cho toàn bộ project
@SpringBootApplication
@EnableScheduling
public class MarketPlaceSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketPlaceSystemApplication.class, args);
    }

}
