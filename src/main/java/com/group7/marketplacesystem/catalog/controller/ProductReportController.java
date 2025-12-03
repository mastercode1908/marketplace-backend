package com.group7.marketplacesystem.catalog.controller;

import com.group7.marketplacesystem.catalog.dto.request.ProductReportRequest;
import com.group7.marketplacesystem.catalog.dto.response.ProductReportResponse;
import com.group7.marketplacesystem.catalog.service.ProductReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/buyer/reports")
public class ProductReportController {

    private final ProductReportService productReportService;

    @PreAuthorize("hasRole('BUYER')")
    @PostMapping("/{product_id}")
    public ResponseEntity<ProductReportResponse> createReview(
            @PathVariable("product_id") Long productId,
            @Valid @RequestBody ProductReportRequest productReportRequest) {

        productReportRequest.setProductId(Integer.parseInt(productId.toString()));

        ProductReportResponse response = productReportService.creatProductReport(productReportRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('BUYER')")
    @GetMapping()
    public ResponseEntity<List<ProductReportResponse>> getAllMyReports() {

        List<ProductReportResponse> response = productReportService.getAllProductReportsByBuyerId();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
