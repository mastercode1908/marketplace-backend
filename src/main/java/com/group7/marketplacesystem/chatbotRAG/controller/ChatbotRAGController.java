package com.group7.marketplacesystem.chatbotRAG.controller;

import com.group7.marketplacesystem.chatbotRAG.dto.request.ChatRequest;
import com.group7.marketplacesystem.chatbotRAG.dto.response.ChatResponse;
import com.group7.marketplacesystem.chatbotRAG.dto.response.ProductInfo;
import com.group7.marketplacesystem.chatbotRAG.dto.request.SearchRequest;
import com.group7.marketplacesystem.chatbotRAG.service.ChatbotRAGService;
import com.group7.marketplacesystem.chatbotRAG.service.ProductVectorService;
import com.group7.marketplacesystem.chatbotRAG.service.impl.ProductVectorServiceImpl;
import com.group7.marketplacesystem.chatbotRAG.service.impl.ChatbotRAGServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for RAG (Retrieval-Augmented Generation) endpoints.
 * Provides AI-powered chat and semantic search for products.
 */
@Slf4j
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatbotRAGController {

    private final ChatbotRAGService ragService;
    private final ProductVectorService productVectorService;

    /**
     * AI Chatbot endpoint with RAG.
     * POST /api/rag/chat
     * 
     * @param request Chat request with user message
     * @return ChatResponse with AI response and relevant products
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat request: {}", request.getMessage());

        try {
            ChatResponse response = ragService.generateResponse(request.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing chat request: {}", e.getMessage(), e);

            // Return error response
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setMessage("Xin lỗi, hệ thống đang gặp sự cố. Vui lòng thử lại sau.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Semantic search endpoint.
     * POST /api/rag/search
     * 
     * @param request Search request with query and limit
     * @return List of similar products
     */
    @PostMapping("/search")
    public ResponseEntity<List<ProductInfo>> search(@Valid @RequestBody SearchRequest request) {
        log.info("Received search request: query='{}', limit={}",
                request.getQuery(), request.getLimit());

        try {
            List<ProductInfo> products = ragService.searchProducts(
                    request.getQuery(),
                    request.getLimit());
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            log.error("Error processing search request: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Manual sync endpoint (admin only).
     * POST /api/rag/sync
     * 
     * Syncs all approved products to vector database.
     * 
     * @return Sync result with count
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncProducts() {
        log.info("Received manual sync request");

        try {
            int syncedCount = productVectorService.syncAllProducts();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product sync completed");
            response.put("syncedCount", syncedCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error syncing products: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Sync failed: " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Sync single product endpoint.
     * POST /api/rag/sync/{productId}
     * 
     * @param productId Product ID to sync
     * @return Sync result
     */
    @PostMapping("/sync/{productId}")
    public ResponseEntity<Map<String, Object>> syncSingleProduct(@PathVariable Integer productId) {
        log.info("Received sync request for product {}", productId);

        try {
            productVectorService.syncProductToVector(productId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product synced successfully");
            response.put("productId", productId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error syncing product {}: {}", productId, e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Sync failed: " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get vector database statistics.
     * GET /api/rag/stats
     * 
     * @return Stats about vector coverage
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, String>> getStats() {
        try {
            String stats = productVectorService.getVectorStats();

            Map<String, String> response = new HashMap<>();
            response.put("stats", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Health check endpoint.
     * GET /api/rag/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "RAG Service");
        return ResponseEntity.ok(response);
    }
}
