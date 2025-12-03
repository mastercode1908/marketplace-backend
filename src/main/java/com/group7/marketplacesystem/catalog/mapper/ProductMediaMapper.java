package com.group7.marketplacesystem.catalog.mapper;

import com.group7.marketplacesystem.catalog.dto.request.ProductCreateRequest;
import com.group7.marketplacesystem.catalog.dto.request.ProductMediaRequest;
import com.group7.marketplacesystem.catalog.dto.response.ProductMediaResponse;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Productmedia;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ProductMediaMapper {

    public static Productmedia toEntity(Product product, ProductMediaRequest request) {
        Productmedia media = new Productmedia();

        media.setProduct(product);
        media.setUrl(request.getUrl());
        media.setPublic_id(request.getPublicId());
        media.setMediaType(request.getType());
        media.setPosition(
                request.getPosition() != null ? request.getPosition() : 0
        );

        media.setCreatedAt(Instant.now());
        media.setDeletedAt(null);

        return media;
    }

    public static ProductMediaResponse toMediaResponse(Productmedia media) {
        if (media == null) return null;

        ProductMediaResponse response = new ProductMediaResponse();
        response.setMediaId(media.getId());
        response.setUrl(media.getUrl());
        response.setPublicId(media.getPublic_id());
        response.setMediaType(media.getMediaType());
        response.setPosition(media.getPosition());
        response.setCreatedAt(media.getCreatedAt());
        response.setDeletedAt(media.getDeletedAt());

        return response;
    }
    public static List<Productmedia> toEntityList(Product product, List<ProductMediaRequest> mediaRequests) {
        List<Productmedia> list = new ArrayList<>();
        if (mediaRequests != null) {
            for (ProductMediaRequest req : mediaRequests) {
                list.add(toEntity(product, req));
            }
        }
        return list;
    }
    public static List<ProductMediaResponse> toMediaResponseList(List<Productmedia> mediaList) {
        List<ProductMediaResponse> list = new ArrayList<>();
        if (mediaList != null) {
            for (Productmedia media : mediaList) {
                list.add(toMediaResponse(media));
            }
        }
        return list;
    }
    public static List<Productmedia> updateMedia(Product product, List<ProductMediaRequest> mediaRequests) {
        return ProductMediaMapper.toEntityList(product, mediaRequests);
    }
}
