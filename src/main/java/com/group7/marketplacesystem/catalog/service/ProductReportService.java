package com.group7.marketplacesystem.catalog.service;

import com.group7.marketplacesystem.catalog.dto.request.ProductReportRequest;
import com.group7.marketplacesystem.catalog.dto.response.ProductReportResponse;

import java.util.List;

public interface ProductReportService {
    ProductReportResponse creatProductReport(ProductReportRequest dto);

//    ProductResponse getProductById(Integer id);

    List<ProductReportResponse> getAllProductReportsByBuyerId();

    List<ProductReportResponse> getAllProductReports();

    ProductReportResponse updateProductReport(Long id, ProductReportRequest dto);
}
