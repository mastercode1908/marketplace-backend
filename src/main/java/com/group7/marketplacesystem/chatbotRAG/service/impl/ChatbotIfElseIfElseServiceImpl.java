package com.group7.marketplacesystem.chatbotRAG.service.impl;

import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.chatbotRAG.service.ChatbotIfElseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotIfElseIfElseServiceImpl implements ChatbotIfElseService {

    private final ProductRepository productRepository;

    @Override
    public String processMessage(String message) {
        String lowerMsg = message.toLowerCase().trim();

        if (lowerMsg.contains("hello") || lowerMsg.contains("hi") || lowerMsg.contains("xin chào")
                || lowerMsg.contains("chào")) {
            return "Xin chào! Tôi là trợ lý ảo của MarketPlace. Tôi có thể giúp bạn tìm sản phẩm hoặc kiểm tra giá cả.";
        }

        if (lowerMsg.contains("giá") || lowerMsg.contains("price") || lowerMsg.contains("tìm")
                || lowerMsg.contains("find")) {
            String keyword = extractKeyword(lowerMsg);
            String productName = extractProductName(lowerMsg, keyword);

            if (productName.isEmpty()) {
                return "Bạn muốn tìm sản phẩm nào?";
            }

            List<Product> products = productRepository.findByNameContainingIgnoreCaseAndDeletedAtIsNull(productName);

            if (products.isEmpty()) {
                return "Xin lỗi, tôi không tìm thấy sản phẩm nào có tên '" + productName + "'.";
            }

            String response = "Tôi tìm thấy " + products.size() + " sản phẩm:\n";
            String productList = products.stream()
                    .limit(5)
                    .map(p -> "- " + p.getName() + ": " + String.format("%,.0f", p.getPrice()) + " VND")
                    .collect(Collectors.joining("\n"));

            return response + productList;
        }

        if (lowerMsg.contains("mua") || lowerMsg.contains("buy")) {
            String keyword = lowerMsg.contains("mua") ? "mua" : "buy";
            String productName = extractProductName(lowerMsg, keyword);

            if (productName.isEmpty()) {
                return "Bạn muốn mua sản phẩm nào?";
            }

            List<Product> products = productRepository.findByNameContainingIgnoreCaseAndDeletedAtIsNull(productName);

            if (products.isEmpty()) {
                return "Xin lỗi, tôi không tìm thấy sản phẩm '" + productName + "' để mua.";
            }

            Product p = products.get(0);
            return "Bạn có thể xem và mua sản phẩm '" + p.getName() + "' tại: /product/" + p.getId();
        }

        return "Xin lỗi, tôi chưa hiểu câu hỏi của bạn. Bạn có thể hỏi về giá sản phẩm, tìm sản phẩm hoặc muốn mua sản phẩm.";
    }

    private String extractKeyword(String message) {
        if (message.contains("giá"))
            return "giá";
        if (message.contains("price"))
            return "price";
        if (message.contains("tìm"))
            return "tìm";
        if (message.contains("find"))
            return "find";
        return "";
    }

    private String extractProductName(String message, String keyword) {
        if (keyword.isEmpty())
            return "";

        int index = message.indexOf(keyword);
        if (index == -1)
            return "";

        String productName = message.substring(index + keyword.length()).trim();
        return productName;
    }
}
