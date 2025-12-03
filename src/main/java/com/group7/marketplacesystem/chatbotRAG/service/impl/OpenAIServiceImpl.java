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
                    .maxTokens(500)
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
     * @return AI response text
     */
    @Override
    public String generateContextualResponse(String userMessage, String context) {
        if (mockMode) {
            log.warn("MOCK MODE: Generating dummy contextual response");
            return generateMockContextualResponse(userMessage, context);
        }

        List<ChatMessage> messages = new ArrayList<>();

        // System message with context - improved prompt
        String systemPrompt = "Bạn là trợ lý mua sắm thân thiện và chuyên nghiệp của OnlineMarketPlace. " +
                "Nhiệm vụ của bạn là tư vấn sản phẩm cho khách hàng một cách tự nhiên và hữu ích.\n\n" +
                "THÔNG TIN SẢN PHẨM:\n" +
                context + "\n\n" +
                "HƯỚNG DẪN TRẢ LỜI:\n" +
                "1. Trả lời bằng tiếng Việt tự nhiên, thân thiện như đang trò chuyện\n" +
                "2. KHÔNG SỬ DỤNG ký hiệu markdown như **, ***, __, ##, v.v.\n" +
                "3. Viết văn bản thuần túy, dễ đọc, không cần định dạng đặc biệt\n" +
                "4. Nếu khách hàng hỏi về sản phẩm cụ thể, hãy giới thiệu các sản phẩm phù hợp từ danh sách\n" +
                "5. Nếu có nhiều lựa chọn, gợi ý 2-3 sản phẩm tốt nhất với lý do rõ ràng\n" +
                "6. Nếu thông tin không đủ hoặc không có sản phẩm phù hợp, hãy lịch sự thông báo và gợi ý tìm kiếm khác\n"
                +
                "7. Giữ câu trả lời ngắn gọn, súc tích, khoảng 2-4 câu\n\n" +
                "Hãy trả lời câu hỏi của khách hàng một cách tự nhiên và hữu ích nhất.";

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
    public String generateMockContextualResponse(String userMessage, String context) {
        String[] templates = {
                "Dựa trên yêu cầu của bạn, tôi gợi ý các sản phẩm sau đây phù hợp với nhu cầu %s.",
                "Chúng tôi có một số sản phẩm tuyệt vời cho %s. Hãy xem các gợi ý bên dưới!",
                "Tôi đã tìm thấy một số sản phẩm phù hợp với %s. Bạn có thể xem chi tiết ở bên dưới.",
                "Để đáp ứng nhu cầu %s, tôi có những gợi ý sau cho bạn.",
                "Các sản phẩm dưới đây sẽ phù hợp với yêu cầu %s của bạn."
        };

        Random random = new Random();
        String template = templates[random.nextInt(templates.length)];

        // Extract simple description from user message
        String description = userMessage.toLowerCase()
                .replace("tìm", "")
                .replace("mua", "")
                .replace("cần", "")
                .replace("muốn", "")
                .trim();

        return String.format(template, description);
    }
}
