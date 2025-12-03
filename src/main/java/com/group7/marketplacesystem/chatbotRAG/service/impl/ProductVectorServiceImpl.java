package com.group7.marketplacesystem.chatbotRAG.service.impl;

import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.chatbotRAG.entity.ProductVector;
import com.group7.marketplacesystem.chatbotRAG.repository.ProductVectorRepository;
import com.group7.marketplacesystem.chatbotRAG.service.OpenAIService;
import com.group7.marketplacesystem.chatbotRAG.service.ProductVectorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing product vectors.
 * Syncs products from MySQL to PostgreSQL with embeddings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductVectorServiceImpl implements ProductVectorService {

    private final ProductVectorRepository productVectorRepository;
    private final ProductRepository productRepository;
//    private final GeminiAIServiceImpl geminiAIService;
    private final OpenAIService openAIService;

    /**
     * Sync a single product to vector database.
     * Creates embedding from product name + description.
     * 
     * @param productId Product ID from MySQL
     * @return Synced ProductVector
     */
    @Override
    @Transactional
    public ProductVector syncProductToVector(Integer productId) {
        log.info("Syncing product {} to vector database", productId);

        // Get product from MySQL
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            log.warn("Product {} not found in MySQL", productId);
            throw new RuntimeException("Product not found: " + productId);
        }

        Product product = productOpt.get();

        // Skip if product is deleted or not approved
        if (product.getDeletedAt() != null || !"Approved".equals(product.getProductStatus())) {
            log.info("Skipping product {} - deleted or not approved", productId);
            return null;
        }

        // Build text for embedding
        String textToEmbed = buildProductText(product);

        // Generate embedding
        double[] embedding = openAIService.generateEmbedding(textToEmbed);

        // Check if vector already exists
        Optional<ProductVector> existingVector = productVectorRepository.findByProductId(productId);
        ProductVector productVector;

        if (existingVector.isPresent()) {
            // Update existing
            productVector = existingVector.get();
            log.info("Updating existing vector for product {}", productId);
        } else {
            // Create new
            productVector = new ProductVector();
            productVector.setProductId(productId);
            log.info("Creating new vector for product {}", productId);
        }

        productVector.setName(product.getName());
        productVector.setDescription(product.getDescription());
        productVector.setEmbeddingFromDoubles(embedding);

        // Use native query with CAST to save
        productVectorRepository.upsertProductVector(
                productId,
                product.getName(),
                product.getDescription(),
                productVector.getEmbedding());

        log.info("Successfully synced product {} to vector database", productId);

        return productVector;
    }

    /**
     * Sync all approved products to vector database.
     * Can be called on startup or manually.
     * 
     * @return Number of products synced
     */
    @Override
    @Transactional
    public int syncAllProducts() {
        log.info("Starting full product sync to vector database");

        List<Product> products = productRepository.findAll();
        int synced = 0;
        int skipped = 0;
        int failed = 0;

        for (Product product : products) {
            try {
                if (product.getDeletedAt() != null || !"Approved".equals(product.getProductStatus())) {
                    skipped++;
                    continue;
                }

                syncProductToVector(product.getId());
                synced++;

                // Small delay to avoid Gemini rate limits
                if (synced % 10 == 0) {
                    Thread.sleep(1000);
                    log.info("Synced {} products so far...", synced);
                }

            } catch (Exception e) {
                log.error("Failed to sync product {}: {}", product.getId(), e.getMessage());
                failed++;
            }
        }

        log.info("Product sync complete. Synced: {}, Skipped: {}, Failed: {}", synced, skipped, failed);
        return synced;
    }

    /**
     * Delete product vector when product is deleted.
     * 
     * @param productId Product ID
     */
    @Override
    @Transactional
    public void deleteProductVector(Integer productId) {
        log.info("Deleting vector for product {}", productId);
        productVectorRepository.deleteByProductId(productId);
    }

    /**
     * Get statistics about vector database.
     */
    @Override
    public String getVectorStats() {
        long totalVectors = productVectorRepository.countAll();
        long totalProducts = productRepository.count();
        return String.format("Vectors: %d, Products: %d, Coverage: %.1f%%",
                totalVectors, totalProducts,
                (totalVectors * 100.0 / Math.max(totalProducts, 1)));
    }

    /**
     * Build text representation of product for embedding.
     */
    @Override
    public String buildProductText(Product product) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sản phẩm: ").append(product.getName()).append(". ");

        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            sb.append("Mô tả: ").append(product.getDescription()).append(". ");
        }

        if (product.getPrice() != null) {
            sb.append("Giá: ").append(product.getPrice()).append(" VND. ");
        }

        if (product.getCategory() != null && product.getCategory().getName() != null) {
            sb.append("Danh mục: ").append(product.getCategory().getName()).append(".");
        }

        return sb.toString();
    }
}
