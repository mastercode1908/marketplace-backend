package com.group7.marketplacesystem.common.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model.embedding:text-embedding-004}")
    private String embeddingModel;

    @Value("${gemini.model.chat:gemini-1.5-flash-latest}")
    private String chatModel;

    @Value("${gemini.mock.mode:true}")
    private boolean mockMode;

    @Bean
    public String geminiApiKey() {
        return apiKey;
    }

    @Bean
    public String geminiEmbeddingModel() {
        return embeddingModel;
    }

    @Bean
    public String geminiChatModel() {
        return chatModel;
    }

    @Bean
    public boolean geminiMockMode() {
        return mockMode;
    }

    @Bean
    public OkHttpClient geminiHttpClient() {
        return new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
                .connectTimeout(Duration.ofSeconds(60))
                .build();
    }
}
