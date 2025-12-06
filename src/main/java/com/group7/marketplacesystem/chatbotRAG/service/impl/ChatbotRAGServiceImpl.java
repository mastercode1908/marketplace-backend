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
    private final OpenAIService openAIService;

    @Override
    public List<ProductInfo> searchProducts(String query, int topK) {
        log.info("Searching products for query: '{}', topK: {}", query, topK);

        try {
            double[] queryEmbedding = openAIService.generateEmbedding(query);
            String embeddingVector = convertToVectorString(queryEmbedding);
            List<ProductVector> similarVectors = productVectorRepository.findTopKSimilar(embeddingVector, topK);

            log.info("Found {} similar products", similarVectors.size());

            return similarVectors.stream()
                    .map(this::convertToProductInfo)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search products: " + e.getMessage(), e);
        }
    }

    @Override
    public ChatResponse generateResponse(String userMessage) {
        log.info("Generating RAG response for message: '{}'", userMessage);

        try {
            boolean needsProductSearch = shouldSearchProducts(userMessage);
            List<ProductInfo> relevantProducts = new ArrayList<>();

            if (needsProductSearch) {
                relevantProducts = searchProducts(userMessage, 3);
            }

            String context = buildContext(relevantProducts);

            String aiResponse;
            if (needsProductSearch && relevantProducts.isEmpty()) {
                aiResponse = "Xin lỗi, tôi không tìm thấy sản phẩm phù hợp với yêu cầu của bạn. " +
                        "Bạn có thể thử mô tả chi tiết hơn hoặc tìm kiếm theo danh mục khác.";
            } else {
                aiResponse = openAIService.generateContextualResponse(userMessage, context,
                        !relevantProducts.isEmpty());
            }

            // Parse selected IDs and filter products
            if (aiResponse.contains("SELECTED_IDS:")) {
                List<Integer> selectedIds = extractSelectedIds(aiResponse);
                if (!selectedIds.isEmpty()) {
                    relevantProducts = relevantProducts.stream()
                            .filter(p -> selectedIds.contains(p.getProductId()))
                            .collect(Collectors.toList());
                }

                // Remove the technical tag from response
                aiResponse = aiResponse.substring(0, aiResponse.indexOf("SELECTED_IDS:")).trim();
            }

            ChatResponse response = new ChatResponse();
            response.setMessage(aiResponse);
            response.setProducts(relevantProducts);

            log.info("Successfully generated RAG response with {} products", relevantProducts.size());
            return response;

        } catch (Exception e) {
            log.error("Error generating RAG response: {}", e.getMessage(), e);
            ChatResponse fallbackResponse = new ChatResponse();
            fallbackResponse.setMessage("Xin lỗi, hiện tại hệ thống đang gặp sự cố. Vui lòng thử lại sau.");
            fallbackResponse.setProducts(new ArrayList<>());
            return fallbackResponse;
        }
    }

    @Override
    public String buildContext(List<ProductInfo> products) {
        if (products.isEmpty()) {
            return "Không có sản phẩm nào phù hợp.";
        }

        StringBuilder context = new StringBuilder();
        context.append("Các sản phẩm có sẵn:\n\n");

        for (int i = 0; i < products.size(); i++) {
            ProductInfo p = products.get(i);
            // Important: Include ID so AI can select it
            context.append(String.format("ID: %d | %s\n", p.getProductId(), p.getName()));

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

    private boolean shouldSearchProducts(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.matches(".*\\b(xin chào|chào|hello|hi|hey|tạm biệt|bye|cảm ơn|thanks|được|ok|ừ)\\b.*")) {
            return false;
        }

        if (lowerMessage.contains("bạn là ai") || lowerMessage.contains("làm gì") ||
                lowerMessage.contains("giúp gì") || lowerMessage.contains("có thể")) {
            return false;
        }

        if (lowerMessage.matches(".*\\b(đẹp trai|xinh|ngầu|tốt|tệ)\\b.*")) {
            return false;
        }

        if (lowerMessage
                .matches(".*\\b(tìm|mua|cần|muốn|có|bán|giá|sản phẩm|hàng|laptop|điện thoại|giày|áo|quần)\\b.*")) {
            return true;
        }

        return true;
    }

    private List<Integer> extractSelectedIds(String aiResponse) {
        List<Integer> ids = new ArrayList<>();
        try {
            if (aiResponse.contains("SELECTED_IDS:")) {
                String idPart = aiResponse.substring(aiResponse.lastIndexOf("SELECTED_IDS:"));
                String jsonArray = idPart.replace("SELECTED_IDS:", "").trim();
                int start = jsonArray.indexOf("[");
                int end = jsonArray.indexOf("]");

                if (start >= 0 && end > start) {
                    jsonArray = jsonArray.substring(start, end + 1);
                    String[] parts = jsonArray.substring(1, jsonArray.length() - 1).split(",");
                    for (String part : parts) {
                        try {
                            if (!part.trim().isEmpty()) {
                                ids.add(Integer.parseInt(part.trim()));
                            }
                        } catch (NumberFormatException e) {
                            // ignore invalid numbers
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing selected IDs: {}", e.getMessage());
        }
        return ids;
    }

    @Override
    public ProductInfo convertToProductInfo(ProductVector vector) {
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

            List<Productmedia> mediaList = productmediaRepository
                    .findByProductIdAndDeletedAtIsNull(vector.getProductId());
            if (!mediaList.isEmpty()) {
                Productmedia firstImage = mediaList.stream()
                        .filter(m -> "image".equalsIgnoreCase(m.getMediaType()))
                        .findFirst()
                        .orElse(mediaList.get(0));
                info.setImageUrl(firstImage.getUrl());
            }
        }

        return info;
    }

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
