package com.group7.marketplacesystem.catalog.mapper;

import com.group7.marketplacesystem.catalog.dto.response.ProductReportResponse;
import com.group7.marketplacesystem.catalog.entity.Productreport;
import org.springframework.stereotype.Component;

import com.group7.marketplacesystem.catalog.repository.ProductmediaRepository;
import com.group7.marketplacesystem.catalog.entity.Productmedia;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductReportMapper {

    private final ProductmediaRepository productmediaRepository;

    public ProductReportResponse toResponse(Productreport productreport) {
        if (productreport == null) {
            return null;
        }
        ProductReportResponse response = new ProductReportResponse();
        response.setId(productreport.getId());
        response.setProductId(productreport.getProduct().getId());
        response.setBuyerId(productreport.getBuyer().getId());
        response.setReason(productreport.getReason());
        response.setStatus(productreport.getStatus());
        response.setCreatedAt(productreport.getCreatedAt());
        response.setResolvedAt(productreport.getResolvedAt());
        response.setShop_name(productreport.getProduct().getSeller().getShopName());
        response.setSellerId(productreport.getProduct().getSeller().getId());
        response.setProduct_name(productreport.getProduct().getName());
        response.setRating_count(productreport.getProduct().getSeller().getRatingCount());
        
        if (productreport.getBuyer() != null && productreport.getBuyer().getUsers() != null) {
            response.setBuyerName(productreport.getBuyer().getUsers().getFullName());
        }

        List<Productmedia> productMedia = productmediaRepository.findByProductId(productreport.getProduct().getId());
        if (!productMedia.isEmpty()) {
            response.setProductImage(productMedia.get(0).getUrl());
        }

        if (productreport.getMedia() != null) {
            List<com.group7.marketplacesystem.catalog.dto.common.ReportMediaDTO> mediaDTOs = productreport.getMedia().stream()
                    .map(m -> com.group7.marketplacesystem.catalog.dto.common.ReportMediaDTO.builder()
                            .url(m.getUrl())
                            .publicId(m.getPublicId())
                            .mediaType(m.getMediaType())
                            .build())
                    .collect(java.util.stream.Collectors.toList());
            response.setMedia(mediaDTOs);
        }

        return response;
    }
}
