package com.group7.marketplacesystem.chatbotRAG.service.impl;

import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Productmedia;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.catalog.repository.ProductmediaRepository;
import com.group7.marketplacesystem.chatbotRAG.dto.response.ChatResponse;
import com.group7.marketplacesystem.chatbotRAG.dto.response.ProductInfo;
import com.group7.marketplacesystem.chatbotRAG.entity.ProductVector;
import com.group7.marketplacesystem.chatbotRAG.repository.ProductVectorRepository;
import com.group7.marketplacesystem.chatbotRAG.service.ChatbotRAGService;
import com.group7.marketplacesystem.chatbotRAG.service.GeminiAIService;
import com.group7.marketplacesystem.chatbotRAG.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core RAG (Retrieval-Augmented Generation) Service.
 * Combines vector search with LLM generation for intelligent product
 * recommendations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotRAGServiceImpl implements ChatbotRAGService {

    private final ProductVectorRepository productVectorRepository;
    private final ProductRepository productRepository;
    private final ProductmediaRepository productmediaRepository;
    // private final GeminiAIService geminiAIService;
    private final OpenAIService openAIService;

    /**
     * Semantic search for products using vector similarity.
     * 
     * @param query User's search query
     * @param topK  Number of results to return
     * @return List of similar products
     */
    @Override
    public List<ProductInfo> searchProducts(String query, int topK) {
        log.info("Searching products for query: '{}', topK: {}", query, topK);

        try {
            // Generate embedding for query
            double[] queryEmbedding = openAIService.generateEmbedding(query);

            // Convert to pgvector format
            String embeddingVector = convertToVectorString(queryEmbedding);

            // Find similar vectors
            List<ProductVector> similarVectors = productVectorRepository.findTopKSimilar(
                    embeddingVector, topK);

            log.info("Found {} similar products", similarVectors.size());

            // Convert to ProductInfo DTOs
            return similarVectors.stream()
                    .map(this::convertToProductInfo)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search products: " + e.getMessage(), e);
        }
    }

    /**
     * Generate chatbot response using RAG pipeline.
     * 
     * @param userMessage User's message
     * @return ChatResponse with AI message and relevant products
     */
    @Override
    public ChatResponse generateResponse(String userMessage) {
        log.info("Generating RAG response for message: '{}'", userMessage);

        try {
            // Step 1: Classify query to determine if product search is needed
            boolean needsProductSearch = shouldSearchProducts(userMessage);
            List<ProductInfo> relevantProducts = new ArrayList<>();

            if (needsProductSearch) {
                // Only search products if query is product-related
                relevantProducts = searchProducts(userMessage, 3); // Reduced from 5 to 3
            }

            // Step 2: Build context from retrieved products (only if any found)
            String context = buildContext(relevantProducts);

            // Step 3: Generate response with context
            String aiResponse;
            if (needsProductSearch && relevantProducts.isEmpty()) {
                aiResponse = "Xin lỗi, tôi không tìm thấy sản phẩm phù hợp với yêu cầu của bạn. " +
                        "Bạn có thể thử mô tả chi tiết hơn hoặc tìm kiếm theo danh mục khác.";
            } else {
                aiResponse = openAIService.generateContextualResponse(userMessage, context,
                        !relevantProducts.isEmpty());
            }

            // Step 4: Build response
            ChatResponse response = new ChatResponse();
            response.setMessage(aiResponse);
            response.setProducts(relevantProducts);

            log.info("Successfully generated RAG response with {} products", relevantProducts.size());
            return response;

        } catch (Exception e) {
            log.error("Error generating RAG response: {}", e.getMessage(), e);

            // Fallback response
            ChatResponse fallbackResponse = new ChatResponse();
            fallbackResponse.setMessage(
                    "Xin lỗi, hiện tại hệ thống đang gặp sự cố. Vui lòng thử lại sau hoặc liên hệ hỗ trợ.");
            fallbackResponse.setProducts(new ArrayList<>());
            return fallbackResponse;
        }
    }

    /**
     * Build context string from retrieved products.
     */
    @Override
    public String buildContext(List<ProductInfo> products) {
        if (products.isEmpty()) {
            return "Không có sản phẩm nào phù hợp.";
        }

        StringBuilder context = new StringBuilder();
        context.append("Các sản phẩm có sẵn:\n\n");

        for (int i = 0; i < products.size(); i++) {
            ProductInfo p = products.get(i);
            context.append(String.format("%d. %s\n", i + 1, p.getName()));

            if (p.getDescription() != null && !p.getDescription().isEmpty()) {
                context.append("   Mô tả: ").append(p.getDescription()).append("\n");
            }

            if (p.getPrice() != null) {
                context.append("   Giá: ").append(p.getPrice()).append(" VND\n");
            }

            if (p.getStockQuantity() != null) {
                context.append("   Còn lại: ").append(p.getStockQuantity()).append(" sản phẩm\n");
            }

            context.append("\n");
        }

        return context.toString();
    }

    /**
     * Determine if query needs product search.
     * Returns true for product-related queries, false for greetings/general
     * questions.
     */
    private boolean shouldSearchProducts(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        // Greetings and general conversation - no products needed
        if (lowerMessage.matches(".*\\b(xin chào|chào|hello|hi|hey|tạm biệt|bye|cảm ơn|thanks|được|ok|ừ)\\b.*")) {
            return false;
        }

        // Questions about how chatbot works - no products
        if (lowerMessage.contains("bạn là ai") || lowerMessage.contains("làm gì") ||
                lowerMessage.contains("giúp gì") || lowerMessage.contains("có thể")) {
            return false;
        }

        // Product-related keywords
        if (lowerMessage
                .matches(".*\\b(tìm|mua|cần|muốn|có|bán|giá|sản phẩm|hàng|laptop|điện thoại|giày|áo|quần)\\b.*")) {
            return true;
        }

        if (lowerMessage.matches(".*\\b(đẹp trai|xinh|ngầu|tốt|tệ)\\b.*")) {
            return false; // Đây là câu hỏi về người dùng, không liên quan sản phẩm
        }

        // Default: search products (better to show than not show)
        return true;
    }

    /**
     * Convert ProductVector to ProductInfo DTO.
     */
    @Override
    public ProductInfo convertToProductInfo(ProductVector vector) {
        // Try to get full product details from MySQL
        Product product = productRepository.findById(vector.getProductId()).orElse(null);

        ProductInfo info = new ProductInfo();
        info.setProductId(vector.getProductId());
        info.setName(vector.getName());
        info.setDescription(vector.getDescription());

        if (product != null) {
            info.setPrice(product.getPrice());
            info.setStockQuantity(product.getStockQuantity());
            info.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
            info.setSellerName(product.getSeller() != null &&
                    product.getSeller().getShopName() != null ? product.getSeller().getShopName() : null);

            // Fetch and set product image
            List<Productmedia> mediaList = productmediaRepository
                    .findByProductIdAndDeletedAtIsNull(vector.getProductId());
            if (!mediaList.isEmpty()) {
                // Get the first image (filter by media_type if needed)
                Productmedia firstImage = mediaList.stream()
                        .filter(m -> "image".equalsIgnoreCase(m.getMediaType()))
                        .findFirst()
                        .orElse(mediaList.get(0)); // Fallback to first media if no image found
                info.setImageUrl(firstImage.getUrl());
            }
        }

        return info;
    }

    /**
     * Convert double array to pgvector string format.
     */
    @Override
    public String convertToVectorString(double[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
