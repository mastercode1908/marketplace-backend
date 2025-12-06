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

    /**
     * Generate vector embedding for given text.
     * 
     * @param text Input text to embed
     * @return Embedding vector as double array (dimension 1536)
     */
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

    /**
     * Generate chat response using GPT with given messages.
     * 
     * @param messages Conversation history
     * @return AI response text
     */
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
                    .maxTokens(300) // Reduced from 500 to 300 for shorter responses
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

    /**
     * Generate chat response with context (for RAG).
     * 
     * @param userMessage User's message
     * @param context     Retrieved context from vector search
     * @param hasProducts Whether products are included in context
     * @return AI response text
     */
    @Override
    public String generateContextualResponse(String userMessage, String context, boolean hasProducts) {
        if (mockMode) {
            log.warn("MOCK MODE: Generating dummy contextual response");
            return generateMockContextualResponse(userMessage, context, hasProducts);
        }

        List<ChatMessage> messages = new ArrayList<>();

        // Improved system prompt for better formatting
        String systemPrompt;

        if (hasProducts) {
            systemPrompt = "Bạn là trợ lý mua sắm thân thiện của OnlineMarketPlace.\n\n" +
                    "THÔNG TIN SẢN PHẨM:\n" +
                    context + "\n\n" +
                    "HƯỚNG DẪN TRẢ LỜI:\n" +
                    "- TUYỆT ĐỐI KHÔNG chào khách hàng hoặc viết lời mở đầu kiểu 'Chào bạn'.\n" +
                    "- Trả lời trực tiếp, tự nhiên, thân thiện như đang trò chuyện.\n" +
                    "- Xưng hô dùng 'Bạn'.\n" +
                    "- Liệt kê sản phẩm theo bullet points (•), mỗi sản phẩm 1 block, gồm:\n" +
                    "  • Tên sản phẩm\n" +
                    "    Giá: <giá>\n" +
                    "    Đặc điểm: <đặc điểm>\n" +
                    "- Giữ câu trả lời ngắn gọn, súc tích, khoảng 2-3 câu tổng thể.\n" +
                    "- Nếu muốn, thêm 1 câu dẫn tự nhiên trước danh sách nhưng không chào.";
        } else {
            // No products - general conversation
            systemPrompt = "Bạn là trợ lý mua sắm thân thiện của OnlineMarketPlace.\n\n" +
                    "HƯỚNG DẪN TRẢ LỜI:\n" +
                    "- TUYỆT ĐỐI KHÔNG chào khách hàng hoặc viết lời mở đầu kiểu 'Chào bạn'.\n" +
                    "- Trả lời tự nhiên, thân thiện, như đang trò chuyện với khách hàng.\n" +
                    "- Xưng hô dùng 'Bạn'.\n" +
                    "- Nếu người dùng hỏi về ngoại hình, sở thích, đánh giá cá nhân: trả lời tự nhiên, vui vẻ, thân thiện, không đề cập đến sản phẩm.\n" +
                    "- Giữ câu trả lời ngắn gọn, súc tích, khoảng 2-3 câu tổng thể.\n" +
                    "- Nếu khách hỏi về bạn: Giới thiệu ngắn gọn, thân thiện.\n" +
                    "- Nếu khách cảm ơn: Đáp lại lịch sự, tự nhiên.";
        }

        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));

        return generateChatResponse(messages);
    }

    // ============ MOCK METHODS ============

    /**
     * Generate mock embedding (random but consistent for same text)
     */
    @Override
    public double[] generateMockEmbedding(String text) {
        Random random = new Random(text.hashCode()); // Consistent seed
        double[] embedding = new double[1536];

        for (int i = 0; i < 1536; i++) {
            embedding[i] = (random.nextDouble() - 0.5) * 2; // Range: -1 to 1
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

    /**
     * Generate mock chat response
     */
    @Override
    public String generateMockResponse(List<ChatMessage> messages) {
        String userMessage = messages.get(messages.size() - 1).getContent();
        return "Mock response for: " + userMessage;
    }

    /**
     * Generate mock contextual response
     */
    @Override
    public String generateMockContextualResponse(String userMessage, String context, boolean hasProducts) {
        if (!hasProducts) {
            // Handle greetings and general questions
            if (userMessage.toLowerCase().contains("chào") || userMessage.toLowerCase().contains("hello")) {
                return "Xin chào! Tôi có thể giúp bạn tìm sản phẩm nào hôm nay?";
            }
            if (userMessage.toLowerCase().contains("cảm ơn")) {
                return "Không có gì! Rất vui được giúp bạn.";
            }
            return "Tôi có thể giúp bạn tìm kiếm sản phẩm. Hãy cho tôi biết bạn cần gì!";
        }

        String[] templates = {
                "Có 3 sản phẩm phù hợp:\\n• Option 1 - Giá tốt\\n• Option 2 - Chất lượng cao\\n• Option 3 - Phổ biến nhất",
                "Tôi tìm thấy một số lựa chọn:\\n• Sản phẩm A - Đáng tin cậy\\n• Sản phẩm B - Giá hợp lý\\n• Sản phẩm C - Bán chạy",
        };

        Random random = new Random();
        return templates[random.nextInt(templates.length)];
    }
}
