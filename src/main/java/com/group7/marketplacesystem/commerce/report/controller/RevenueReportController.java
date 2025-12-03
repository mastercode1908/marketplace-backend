package com.group7.marketplacesystem.commerce.report.controller;

import com.group7.marketplacesystem.commerce.report.dto.request.RevenueReportRequest;
import com.group7.marketplacesystem.commerce.report.dto.response.RevenueReportResponse;
import com.group7.marketplacesystem.commerce.report.dto.response.SellerOptionDTO;
import com.group7.marketplacesystem.commerce.report.service.ExcelExportService;
import com.group7.marketplacesystem.commerce.report.service.PdfExportService;
import com.group7.marketplacesystem.commerce.report.service.RevenueReportService;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reports/revenue")
@RequiredArgsConstructor
public class RevenueReportController {

    private final RevenueReportService revenueReportService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<RevenueReportResponse> getReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "daily") String periodType,
            @RequestParam(required = false) Integer sellerId) {
        
        RevenueReportRequest request = RevenueReportRequest.builder()
            .startDate(startDate)
            .endDate(endDate)
            .periodType(periodType)
            .sellerId(sellerId)
            .build();
        
        RevenueReportResponse response = revenueReportService.generateReport(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "daily") String periodType,
            @RequestParam(required = false) Integer sellerId) {
        
        RevenueReportRequest request = RevenueReportRequest.builder()
            .startDate(startDate)
            .endDate(endDate)
            .periodType(periodType)
            .sellerId(sellerId)
            .build();
        
        RevenueReportResponse report = revenueReportService.generateReport(request);
        Workbook workbook = excelExportService.generateExcelReport(report);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            workbook.close();
            
            String filename = generateFilename("revenue_report", startDate, endDate, "xlsx");
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error exporting Excel report", e);
        }
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<byte[]> exportToPdf(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "daily") String periodType,
            @RequestParam(required = false) Integer sellerId) {
        
        RevenueReportRequest request = RevenueReportRequest.builder()
            .startDate(startDate)
            .endDate(endDate)
            .periodType(periodType)
            .sellerId(sellerId)
            .build();
        
        RevenueReportResponse report = revenueReportService.generateReport(request);
        ByteArrayOutputStream baos = pdfExportService.generatePdfReport(report);
        
        String filename = generateFilename("revenue_report", startDate, endDate, "pdf");
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(baos.toByteArray());
    }

    @GetMapping("/sellers")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    public ResponseEntity<List<SellerOptionDTO>> getAllSellers() {
        List<User> sellers = userRepository.findBySeller();
        List<SellerOptionDTO> sellerOptions = sellers.stream()
            .map(user -> {
                Seller seller = sellerRepository.findById(user.getId()).orElse(null);
                String shopName = seller != null && seller.getShopName() != null 
                    ? seller.getShopName() 
                    : user.getEmail();
                return SellerOptionDTO.builder()
                    .sellerId(user.getId())
                    .sellerName(shopName)
                    .sellerEmail(user.getEmail())
                    .build();
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(sellerOptions);
    }

    private String generateFilename(String prefix, LocalDate startDate, LocalDate endDate, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String start = startDate != null ? startDate.format(formatter) : "all";
        String end = endDate != null ? endDate.format(formatter) : "all";
        return String.format("%s_%s_%s.%s", prefix, start, end, extension);
    }
}

