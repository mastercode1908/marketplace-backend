package com.group7.marketplacesystem.catalog.service;

import com.group7.marketplacesystem.catalog.dto.request.ProductRequest;
import com.group7.marketplacesystem.catalog.dto.response.ProductDetailResponse;
import com.group7.marketplacesystem.catalog.dto.request.ProductCreateRequest;
import com.group7.marketplacesystem.catalog.dto.response.ProductInfoResponse;
import com.group7.marketplacesystem.catalog.dto.response.ProductResponse;
import com.group7.marketplacesystem.catalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface ProductService {

    ProductInfoResponse createProduct(ProductCreateRequest request);


    ProductInfoResponse getProductById(Integer productId);

    List<ProductDetailResponse> getAllPendingProduct();
    List<ProductDetailResponse> getProductsByStatus(String status);

    ProductDetailResponse updateStatusProduct(Long id, ProductRequest dto);

    ProductInfoResponse updateProduct(Integer productId, ProductCreateRequest request);


    void deleteProduct(Integer productId);


    List<ProductInfoResponse> getMyProducts();


    List<ProductInfoResponse> getAllProducts();

    ByteArrayInputStream exportAllProductsToExcel(List<Product> products);

    List<Product> getAllProductsWhereDeletedAtIsNull();
    Page<ProductInfoResponse> getAllProductsRandomized(Pageable pageable);
    Page<ProductInfoResponse> getAllProductsOfShopHasServicePackage(Pageable pageable);

    List<ProductInfoResponse> getProductsBySellerId(Integer sellerId);
    List<ProductInfoResponse> getPromotedProducts();
}
