package com.group7.marketplacesystem.promotion.listener;

import com.group7.marketplacesystem.promotion.repository.PromotionRepository;
import com.group7.marketplacesystem.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionStartupListener {
    private final PromotionService promotionService;

    @EventListener(ApplicationReadyEvent.class) // @EventListener là một annotation của Spring dùng để lắng nghe (listen) các sự kiện (event) xảy ra trong vòng đời của ứng dụng.
                                                // ApplicationReadyEvent.class là một loại event đặc biệt mà Spring Boot phát ra ngay sau khi toàn bộ ứng dụng đã khởi động xong
    public void onStartup() { // khi sự kiện ApplicationReadyEvent xảy ra — tức là ngay sau khi ứng dụng khởi chạy thành công, nó sẽ gọi hàm bên dưới
        promotionService.expirePromotions();
    }
}
