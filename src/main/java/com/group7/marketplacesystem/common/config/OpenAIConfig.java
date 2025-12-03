package com.group7.marketplacesystem.common.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model.embedding}")
    private String embeddingModel;

    @Value("${openai.model.chat}")
    private String chatModel;

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(apiKey, Duration.ofSeconds(60));
    }

    @Bean
    public String embeddingModelName() {
        return embeddingModel;
    }

    @Bean
    public String chatModelName() {
        return chatModel;
    }
}
