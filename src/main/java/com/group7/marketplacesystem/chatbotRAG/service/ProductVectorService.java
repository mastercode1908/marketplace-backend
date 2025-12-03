package com.group7.marketplacesystem.chatbotRAG.service;

import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.chatbotRAG.entity.ProductVector;

public interface ProductVectorService {
    ProductVector syncProductToVector(Integer productId);
    int syncAllProducts();
    void deleteProductVector(Integer productId);
    String getVectorStats();
    String buildProductText(Product product);
}
