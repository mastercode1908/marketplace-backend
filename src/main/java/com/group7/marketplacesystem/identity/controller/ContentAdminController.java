package com.group7.marketplacesystem.identity.controller;

import com.group7.marketplacesystem.catalog.dto.request.ProductReportRequest;
import com.group7.marketplacesystem.catalog.dto.request.ProductRequest;
import com.group7.marketplacesystem.catalog.dto.request.ReviewRequest;
import com.group7.marketplacesystem.catalog.dto.response.ProductDetailResponse;
import com.group7.marketplacesystem.catalog.dto.response.ProductReportResponse;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.service.ProductReportService;
import com.group7.marketplacesystem.catalog.service.ProductService;
import io.jsonwebtoken.io.IOException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ContentAdminController {

    private final ProductService productService;
    private final ProductReportService productReportService;

    @PreAuthorize("hasRole('CONTENTADMIN')")
    @GetMapping("/products/pending")
    public ResponseEntity<List<ProductDetailResponse>> getAllPendingProduct(){
        return ResponseEntity.ok(productService.getAllPendingProduct());
    }

    @PreAuthorize("hasRole('CONTENTADMIN')")
    @GetMapping("/products")
    public ResponseEntity<List<ProductDetailResponse>> getProducts(@RequestParam(required = false) String status){
        return ResponseEntity.ok(productService.getProductsByStatus(status));
    }

    @PreAuthorize("hasRole('CONTENTADMIN')")
    @PutMapping("/product/{product_id}/status")
    public ResponseEntity<ProductDetailResponse> updateStatusProduct(
            @PathVariable("product_id") Integer productId,
            @RequestBody ProductRequest productRequest){
        return ResponseEntity.ok(productService.updateStatusProduct(Long.parseLong(productId.toString()), productRequest));
    }

    @PreAuthorize("hasRole('CONTENTADMIN')")
    @GetMapping("/reports")
    public ResponseEntity<List<ProductReportResponse>> getProductReport() {

        List<ProductReportResponse> response = productReportService.getAllProductReports();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('CONTENTADMIN')")
    @PutMapping("/reports/{productReport_id}")
    public ResponseEntity<ProductReportResponse> updateStatusProductReport(
            @PathVariable("productReport_id") Integer productReportId,
            @RequestBody ProductReportRequest productReportRequest) {

        ProductReportResponse response = productReportService.updateProductReport(Long.parseLong(productReportId.toString()), productReportRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/export/products")
    public ResponseEntity<InputStreamResource> exportProducts() {
        try {
            // Lấy danh sách sản phẩm chưa bị xóa
            List<Product> products = productService.getAllProductsWhereDeletedAtIsNull();

            // Xuất danh sách sản phẩm ra Excel
            ByteArrayInputStream excelFile = productService.exportAllProductsToExcel(products);

            // Tạo InputStreamResource từ ByteArrayInputStream
            InputStreamResource fileResource = new InputStreamResource(excelFile);

            String fileName = "products.xlsx";

            // Trả về ResponseEntity với header và content type đúng
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(fileResource);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
