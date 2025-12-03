package com.group7.marketplacesystem.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 🔓 Cho phép frontend gọi API
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",      // React local
                "http://127.0.0.1:3000",
                "https://separate-ringtone-missed-using.trycloudflare.com"
        ));

        // Cho phép các method cần thiết
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Cho phép gửi các header tuỳ chỉnh (ví dụ Authorization)
        config.setAllowedHeaders(List.of("*"));

        // Cho phép gửi cookie hoặc header Authorization
        config.setAllowCredentials(true);

        // Áp dụng cho toàn bộ API
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source; // trả về CorsConfigurationSource
    }
}

