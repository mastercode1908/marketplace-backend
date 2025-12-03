package com.group7.marketplacesystem.chatbotRAG.service;

import com.group7.marketplacesystem.chatbotRAG.dto.response.ChatResponse;
import com.group7.marketplacesystem.chatbotRAG.dto.response.ProductInfo;
import com.group7.marketplacesystem.chatbotRAG.entity.ProductVector;

import java.util.List;

public interface ChatbotRAGService {
    List<ProductInfo> searchProducts(String query, int topK);
    ChatResponse generateResponse(String userMessage);
    String buildContext(List<ProductInfo> products);
    ProductInfo convertToProductInfo(ProductVector vector);
    String convertToVectorString(double[] embedding);
}
