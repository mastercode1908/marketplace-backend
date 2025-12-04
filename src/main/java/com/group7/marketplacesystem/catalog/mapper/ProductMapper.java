package com.group7.marketplacesystem.catalog.mapper;

import com.group7.marketplacesystem.catalog.dto.request.ProductCreateRequest;
import com.group7.marketplacesystem.catalog.dto.request.ProductRequest;
import com.group7.marketplacesystem.catalog.dto.response.ProductDetailResponse;
import com.group7.marketplacesystem.catalog.dto.response.ProductResponse;
import com.group7.marketplacesystem.catalog.entity.Category;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.repository.ProductmediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import com.group7.marketplacesystem.catalog.dto.response.ProductInfoResponse;
import com.group7.marketplacesystem.catalog.dto.response.ProductMediaResponse;
import com.group7.marketplacesystem.catalog.dto.response.ProductResponse;
import com.group7.marketplacesystem.catalog.entity.Category;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Productmedia;
import com.group7.marketplacesystem.identity.entity.Seller;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.group7.marketplacesystem.catalog.mapper.ProductMediaMapper.toMediaResponseList;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ProductmediaRepository productmediaRepository;

    // Map từ ProductRequest → ProductEntity luu vao db
    public static Product toEntity(ProductRequest dto){
        if(dto == null){
            return null;
        }
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        // Gán category từ categoryId
        if(dto.getCategoryId() != null){
            Category category = new Category();
            category.setId(dto.getCategoryId());
            product.setCategory(category);
        }
        return product;
    }

    // Map từ ProductEntity → ProductResponse tu db phan hoi ra cho ng dung
    public ProductResponse toResponse(Product product){
        if (product == null) {
            return null;
        }
        ProductResponse response = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .productStatus(product.getProductStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
        // Map categoryId nếu category khác null
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
        }
        return response;
    }

    public ProductDetailResponse toProductDetailResponse(Product product){
        if (product == null) {
            return null;
        }

        List<Productmedia> media = productmediaRepository.findByProductId(product.getId());

        List<ProductMediaResponse> mediaResponses = toMediaResponseList(media);

        ProductDetailResponse response = ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .productStatus(product.getProductStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .shop_name(product.getSeller().getShopName())
                .rating_count(product.getSeller().getRatingCount())
                .total_sales(product.getSeller().getTotalSales())
                .media(mediaResponses)
                .email(product.getSeller().getUsers().getEmail())
                .build();
        // Map categoryId nếu category khác null
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }
        return response;
    }

// Tạo entity Product từ request
    public static Product toEntity(ProductCreateRequest request, Seller seller, Category category) {
        Product product = new Product();

        product.setSeller(seller);
        product.setCategory(category);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setWeight(request.getWeight());
        product.setStockQuantity(
                request.getStockQuantity() != null ? request.getStockQuantity() : 0
        );
        product.setProductStatus(
                request.getProductStatus() != null ? request.getProductStatus() : "Pending"
        );

        Instant now = Instant.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        product.setDeletedAt(null);

        return product;
    }

    public static ProductInfoResponse toResponse(Product product, List<Productmedia> mediaList) {
        return toResponse(product, mediaList, null);
    }

    public static ProductInfoResponse toResponse(Product product, List<Productmedia> mediaList, Integer soldQuantity) {
        if (product == null) return null;

        ProductInfoResponse response = new ProductInfoResponse();

        response.setProductId(product.getId());
        response.setSellerId(product.getSeller() != null ? product.getSeller().getId() : null);
        response.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setWeight(product.getWeight());
        response.setStockQuantity(product.getStockQuantity());
        response.setSoldQuantity(soldQuantity != null ? soldQuantity : 0);
        response.setProductStatus(product.getProductStatus());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setDeletedAt(product.getDeletedAt());

        List<ProductMediaResponse> mediaResponses = toMediaResponseList(mediaList);
        response.setMedia(mediaResponses);

        return response;
    }

    public static Product updateEntity(Product product, ProductCreateRequest request, Category category) {
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getWeight() != null) {
            product.setWeight(request.getWeight());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getProductStatus() != null) {
            product.setProductStatus(request.getProductStatus());
        }

        // Cập nhật category nếu khác
        if (category != null) {
            product.setCategory(category);
        }

        // Update updatedAt
        product.setUpdatedAt(Instant.now());

        return product;
    }
}
