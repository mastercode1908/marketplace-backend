package com.group7.marketplacesystem.chatbotRAG.service.impl;

import com.group7.marketplacesystem.chatbotRAG.service.OpenAIService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Service for interacting with OpenAI API.
 * Handles embedding generation and chat completions.
 * 
 * MOCK MODE: Set OPENAI_MOCK_MODE=true in .env to use dummy data
 */
@Slf4j
@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final OpenAiService openAiService;
    private final String embeddingModel;
    private final String chatModel;

    @Value("${openai.mock.mode}")
    private boolean mockMode;

    @Autowired
    public OpenAIServiceImpl(
            OpenAiService openAiService,
            @Qualifier("embeddingModelName") String embeddingModel,
            @Qualifier("chatModelName") String chatModel) {
        this.openAiService = openAiService;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
    }

    @Override
    public double[] generateEmbedding(String text) {
        if (mockMode) {
            log.warn("MOCK MODE: Generating dummy embedding");
            return generateMockEmbedding(text);
        }

        try {
            log.info("Generating embedding for text: {} chars", text.length());

            EmbeddingRequest request = EmbeddingRequest.builder()
                    .model(embeddingModel)
                    .input(Collections.singletonList(text))
                    .build();

            EmbeddingResult result = openAiService.createEmbeddings(request);

            if (result.getData().isEmpty()) {
                log.error("No embedding returned from OpenAI");
                throw new RuntimeException("Failed to generate embedding");
            }

            List<Double> embedding = result.getData().get(0).getEmbedding();
            double[] embeddingArray = new double[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                embeddingArray[i] = embedding.get(i);
            }

            log.info("Successfully generated embedding with dimension: {}", embeddingArray.length);
            return embeddingArray;

        } catch (Exception e) {
            log.error("Error generating embedding: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateChatResponse(List<ChatMessage> messages) {
        if (mockMode) {
            log.warn("MOCK MODE: Generating dummy chat response");
            return generateMockResponse(messages);
        }

        try {
            log.info("Generating chat response with {} messages", messages.size());

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(chatModel)
                    .messages(messages)
                    .temperature(0.7)
                    .maxTokens(400) // Moderate token limit
                    .build();

            ChatCompletionResult result = openAiService.createChatCompletion(request);

            if (result.getChoices().isEmpty()) {
                log.error("No response returned from OpenAI");
                throw new RuntimeException("Failed to generate chat response");
            }

            String response = result.getChoices().get(0).getMessage().getContent();
            log.info("Successfully generated chat response: {} chars", response.length());
            return response;

        } catch (Exception e) {
            log.error("Error generating chat response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate chat response: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateContextualResponse(String userMessage, String context, boolean hasProducts) {
        if (mockMode) {
            log.warn("MOCK MODE: Generating dummy contextual response");
            return generateMockContextualResponse(userMessage, context, hasProducts);
        }

        List<ChatMessage> messages = new ArrayList<>();
        String systemPrompt;

        if (hasProducts) {
            systemPrompt = "Bạn là trợ lý mua sắm thông minh của OnlineMarketPlace.\n\n" +
                    "DỮ LIỆU SẢN PHẨM KHẢ DỤNG:\n" +
                    context + "\n\n" +
                    "NHIỆM VỤ:\n" +
                    "1. Trả lời câu hỏi của khách hàng dựa trên dữ liệu sản phẩm trên. Xưng hô là 'Bạn'.\n" +
                    "2. Nếu khách hỏi mua/tìm sản phẩm: Hãy chọn ra 1-3 sản phẩm phù hợp nhất để giới thiệu.\n" +
                    "3. Format câu trả lời:\n" +
                    "   - Mở đầu 1 câu dẫn tự nhiên (không chào hỏi rườm rà).\n" +
                    "   - Liệt kê sản phẩm theo bullet points (•), mỗi sản phẩm 1 block, gồm:\n" +
                    "  • Tên sản phẩm\n" +
                    "    Giá: <giá>\n" +
                    "    Đặc điểm: <mô tả ngắn gọn, nổi bật nhất>\n" +
                    "4. QUAN TRỌNG NHẤT - SYNC DỮ LIỆU:\n" +
                    "   - Cuối cùng, BẮT BUỘC phải liệt kê danh sách ID của các sản phẩm bạn vừa giới thiệu theo định dạng: SELECTED_IDS: [id1, id2, id3]\n"
                    +
                    "   - Chỉ liệt kê ID của những sản phẩm bạn thực sự nhắc đến trong câu trả lời.\n" +
                    "   - Ví dụ output cuối cùng: ... SELECTED_IDS: [101, 105]";
        } else {
            systemPrompt = "Bạn là trợ lý mua sắm thân thiện của OnlineMarketPlace.\n\n" +
                    "HƯỚNG DẪN:\n" +
                    "- Trả lời tự nhiên, ngắn gọn (2-3 câu).\n" +
                    "- Không chào hỏi kiểu 'Chatbot xin chào'. Xưng hô 'Mình' hoặc 'Bạn'.\n" +
                    "- Nếu khách chào: Chào lại thân thiện.\n" +
                    "- Nếu khách hỏi linh tinh (về bản thân bạn, thời tiết, đùa giỡn): Trả lời vui vẻ, thông minh, không cần lái về bán hàng nếu không liên quan.\n"
                    +
                    "- TUYỆT ĐỐI KHÔNG bịa đặt sản phẩm nếu không có dữ liệu.";
        }

        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));

        return generateChatResponse(messages);
    }

    // ============ MOCK METHODS ============

    @Override
    public double[] generateMockEmbedding(String text) {
        Random random = new Random(text.hashCode());
        double[] embedding = new double[1536];

        for (int i = 0; i < 1536; i++) {
            embedding[i] = (random.nextDouble() - 0.5) * 2;
        }

        // Normalize
        double sum = 0;
        for (double v : embedding)
            sum += v * v;
        double norm = Math.sqrt(sum);
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] /= norm;
        }

        log.info("Generated mock embedding with dimension: {}", embedding.length);
        return embedding;
    }

    @Override
    public String generateMockResponse(List<ChatMessage> messages) {
        String userMessage = messages.get(messages.size() - 1).getContent();
        return "Mock response for: " + userMessage;
    }

    @Override
    public String generateMockContextualResponse(String userMessage, String context, boolean hasProducts) {
        if (!hasProducts) {
            if (userMessage.toLowerCase().contains("chào") || userMessage.toLowerCase().contains("hello")) {
                return "Xin chào! Mình có thể giúp gì cho bạn hôm nay?";
            }
            return "Mình là trợ lý ảo, sẵn sàng hỗ trợ bạn tìm kiếm sản phẩm!";
        }

        // Mock returning IDs
        return "Có vài sản phẩm này hay lắm:\n" +
                "• MacBook Pro M1 - 30tr\n" +
                "• Dell XPS 15 - 35tr\n" +
                "SELECTED_IDS: [1, 2]";
    }
}
