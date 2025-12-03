package com.group7.marketplacesystem.chatbotRAG.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group7.marketplacesystem.chatbotRAG.service.GeminiAIService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Implementation of GeminiAIService using direct HTTP calls to Google Gemini
 * REST API.
 * Similar to OpenAIServiceImpl approach - no SDK, just HTTP client.
 *
 * MOCK MODE: Set GEMINI_MOCK_MODE=true in .env to use dummy data
 */
@Service
public class GeminiAIServiceImpl implements GeminiAIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIServiceImpl.class);

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String embeddingModel;
    private final String chatModel;
    private final boolean mockMode;

    public GeminiAIServiceImpl(
            @Qualifier("geminiHttpClient") OkHttpClient httpClient,
            @Qualifier("geminiApiKey") String apiKey,
            @Qualifier("geminiEmbeddingModel") String embeddingModel,
            @Qualifier("geminiChatModel") String chatModel,
            @Qualifier("geminiMockMode") boolean mockMode) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        // Sanitize model names: remove "models/" prefix if present to avoid duplication
        this.embeddingModel = embeddingModel.replace("models/", "");
        this.chatModel = chatModel.replace("models/", "");
        this.mockMode = mockMode;

//        if (!mockMode) {
//            listAvailableModels();
//        }
    }

    // Danh sach cac model co san
    private void listAvailableModels() {
        try {
            String url = GEMINI_BASE_URL + "/models?key=" + apiKey;
            Request request = new Request.Builder().url(url).get().build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    log.info("Available Gemini Models: {}", response.body().string());
                } else {
                    log.warn("Failed to list models: {} {}", response.code(), response.message());
                }
            }
        } catch (Exception e) {
            log.error("Error listing Gemini models", e);
        }
    }

    @Override
    public double[] generateEmbedding(String text) {
        if (mockMode) {
            log.info("[MOCK MODE] Generating mock embedding for text: {}",
                    text.length() > 50 ? text.substring(0, 50) + "..." : text);
            return generateMockEmbedding(text);
        }

        try {
            log.info("Generating Gemini embedding for text: {}",
                    text.length() > 50 ? text.substring(0, 50) + "..." : text);

            // Build request body - NOTE: Removed "model" field from body as it's in URL
            String requestBody = String.format("""
                    {
                        "content": {
                            "parts": [{
                                "text": "%s"
                            }]
                        }
                    }
                    """, text.replace("\"", "\\\"").replace("\n", "\\n"));

            // Build request URL
            String url = String.format("%s/models/%s:embedContent?key=%s",
                    GEMINI_BASE_URL, embeddingModel, apiKey);

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(requestBody, JSON))
                    .build();

            // Execute request
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    log.error("Gemini API Error: {} {} - Body: {}", response.code(), response.message(), errorBody);
                    throw new RuntimeException(
                            "Gemini API error: " + response.code() + " " + response.message() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                // Parse embedding from response
                JsonNode valuesNode = jsonResponse.path("embedding").path("values");
                if (valuesNode.isMissingNode() || !valuesNode.isArray()) {
                    log.error("Invalid Gemini response: {}", responseBody);
                    throw new RuntimeException("Invalid response format from Gemini API");
                }

                double[] embedding = new double[valuesNode.size()];
                for (int i = 0; i < valuesNode.size(); i++) {
                    embedding[i] = valuesNode.get(i).asDouble();
                }

                log.info("Successfully generated embedding with dimension: {}", embedding.length);
                return embedding;
            }

        } catch (Exception e) {
            log.error("Error generating Gemini embedding", e);
            throw new RuntimeException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateContextualResponse(String userMessage, String context) {
        if (mockMode) {
            log.info("[MOCK MODE] Generating mock contextual response");
            return generateMockContextualResponse(userMessage, context);
        }

        try {
            log.info("Generating Gemini contextual response for message: {}", userMessage);

            String promptText = String.format("""
                    Bạn là trợ lý AI thông minh của một marketplace bán hàng online.
                    Nhiệm vụ của bạn là tư vấn sản phẩm cho khách hàng dựa trên thông tin sau:

                    THÔNG TIN SẢN PHẨM:
                    %s

                    CÂU HỎI CỦA KHÁCH HÀNG:
                    %s

                    Yêu cầu:
                    1. Trả lời câu hỏi một cách ngắn gọn, rõ ràng và thân thiện
                    2. Nếu có sản phẩm phù hợp, hãy giới thiệu chi tiết
                    3. Nếu không có sản phẩm nào phù hợp, hãy gợi ý các sản phẩm tương tự
                    4. Luôn kết thúc bằng câu hỏi để tiếp tục hỗ trợ khách hàng

                    Trả lời:
                    """, context, userMessage);

            // Build request body
            String requestBody = String.format("""
                    {
                        "contents": [{
                            "parts": [{
                                "text": "%s"
                            }]
                        }]
                    }
                    """, promptText.replace("\"", "\\\"").replace("\n", "\\n"));

            // Build request URL
            String url = String.format("%s/models/%s:generateContent?key=%s",
                    GEMINI_BASE_URL, chatModel, apiKey);

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(requestBody, JSON))
                    .build();

            // Execute request
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    log.error("Gemini API Error: {} {} - Body: {}", response.code(), response.message(), errorBody);
                    throw new RuntimeException(
                            "Gemini API error: " + response.code() + " " + response.message() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                // Parse text from response
                JsonNode candidatesNode = jsonResponse.path("candidates");
                if (candidatesNode.isMissingNode() || !candidatesNode.isArray() || candidatesNode.isEmpty()) {
                    log.error("Invalid Gemini response (no candidates): {}", responseBody);
                    throw new RuntimeException("Invalid response format from Gemini API: No candidates");
                }

                JsonNode textNode = candidatesNode.get(0)
                        .path("content")
                        .path("parts").get(0)
                        .path("text");

                if (textNode.isMissingNode()) {
                    log.error("Invalid Gemini response (no text): {}", responseBody);
                    throw new RuntimeException("Invalid response format from Gemini API: No text");
                }

                String result = textNode.asText();
                log.info("Successfully generated contextual response: {}",
                        result.length() > 100 ? result.substring(0, 100) + "..." : result);

                return result;
            }

        } catch (Exception e) {
            log.error("Error generating Gemini contextual response", e);
            throw new RuntimeException("Failed to generate contextual response: " + e.getMessage(), e);
        }
    }

    @Override
    public double[] generateMockEmbedding(String text) {
        log.debug("[MOCK] Generating mock embedding for: {}", text);

        // Use text hash as seed for consistency
        Random random = new Random(text.hashCode());

        // Gemini text-embedding-004 uses 768 dimensions
        double[] embedding = new double[768];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = random.nextGaussian();
        }

        // Normalize the vector
        double norm = 0.0;
        for (double v : embedding) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);

        for (int i = 0; i < embedding.length; i++) {
            embedding[i] /= norm;
        }

        return embedding;
    }

    @Override
    public String generateMockContextualResponse(String userMessage, String context) {
        log.debug("[MOCK] Generating mock contextual response");

        String[] templates = {
                "Cảm ơn bạn đã hỏi về \"%s\". Dựa trên dữ liệu của chúng tôi, tôi tìm thấy một số sản phẩm phù hợp. Bạn có muốn xem chi tiết không?",
                "Tôi hiểu bạn đang tìm kiếm về \"%s\". Chúng tôi có nhiều sản phẩm tốt mà bạn có thể quan tâm. Bạn cần thêm thông tin gì?",
                "Về câu hỏi \"%s\" của bạn, tôi có thể giúp bạn tìm sản phẩm phù hợp. Bạn quan tâm đến mức giá nào?",
                "Dựa trên yêu cầu \"%s\", tôi đã tìm thấy một số gợi ý. Bạn muốn biết thêm về tính năng nào?"
        };

        Random random = new Random(userMessage.hashCode());
        String template = templates[random.nextInt(templates.length)];

        return String.format(template,
                userMessage.length() > 30 ? userMessage.substring(0, 30) + "..." : userMessage);
    }
}
