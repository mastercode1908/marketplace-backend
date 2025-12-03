package com.group7.marketplacesystem.commerce.report.service.impl;

import com.group7.marketplacesystem.commerce.report.dto.response.RevenueByPeriodDTO;
import com.group7.marketplacesystem.commerce.report.dto.response.RevenueBySellerDTO;
import com.group7.marketplacesystem.commerce.report.dto.response.RevenueReportResponse;
import com.group7.marketplacesystem.commerce.report.dto.response.TopRevenueDayDTO;
import com.group7.marketplacesystem.commerce.report.service.ExcelExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public Workbook generateExcelReport(RevenueReportResponse report) {
        Workbook workbook = new XSSFWorkbook();
        
        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);

        // Sheet 1: Tổng quan
        createSummarySheet(workbook, report, headerStyle, titleStyle, currencyStyle, numberStyle);
        
        // Sheet 2: Doanh thu theo kỳ
        createRevenueByPeriodSheet(workbook, report, headerStyle, currencyStyle, numberStyle);
        
        // Sheet 3: Top 10 ngày
        createTopRevenueDaysSheet(workbook, report, headerStyle, currencyStyle, numberStyle);
        
        // Sheet 4: Doanh thu theo seller (nếu có)
        if (!report.getRevenueBySeller().isEmpty()) {
            createRevenueBySellerSheet(workbook, report, headerStyle, currencyStyle, numberStyle);
        }

        return workbook;
    }

    private void createSummarySheet(Workbook workbook, RevenueReportResponse report,
                                   CellStyle headerStyle, CellStyle titleStyle,
                                   CellStyle currencyStyle, CellStyle numberStyle) {
        Sheet sheet = workbook.createSheet("Tổng quan");

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO DOANH THU");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowNum++; // Empty row

        // Period info
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue("Từ ngày:");
        periodRow.createCell(1).setCellValue(report.getStartDate().format(DATE_FORMATTER));
        periodRow.createCell(2).setCellValue("Đến ngày:");
        periodRow.createCell(3).setCellValue(report.getEndDate().format(DATE_FORMATTER));

        rowNum++; // Empty row

        // Summary headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Chỉ tiêu");
        headerRow.createCell(1).setCellValue("Giá trị");
        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.getCell(1).setCellStyle(headerStyle);

        // Summary data
        createSummaryRow(sheet, rowNum++, "Tổng doanh thu", report.getTotalRevenue(), currencyStyle);
        createSummaryRow(sheet, rowNum++, "Tổng số đơn hàng", BigDecimal.valueOf(report.getTotalOrders()), numberStyle);
        createSummaryRow(sheet, rowNum++, "Tổng tiền COD", report.getTotalCodAmount(), currencyStyle);
        createSummaryRow(sheet, rowNum++, "Tổng tiền Online", report.getTotalOnlineAmount(), currencyStyle);

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createRevenueByPeriodSheet(Workbook workbook, RevenueReportResponse report,
                                           CellStyle headerStyle, CellStyle currencyStyle,
                                           CellStyle numberStyle) {
        Sheet sheet = workbook.createSheet("Doanh thu theo kỳ");

        int rowNum = 0;

        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Kỳ");
        headerRow.createCell(1).setCellValue("Doanh thu");
        headerRow.createCell(2).setCellValue("Số đơn hàng");
        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.getCell(1).setCellStyle(headerStyle);
        headerRow.getCell(2).setCellStyle(headerStyle);

        // Data
        for (RevenueByPeriodDTO dto : report.getRevenueByPeriod()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getPeriodLabel());
            
            Cell revenueCell = row.createCell(1);
            revenueCell.setCellValue(dto.getRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);
            
            Cell orderCell = row.createCell(2);
            orderCell.setCellValue(dto.getOrderCount());
            orderCell.setCellStyle(numberStyle);
        }

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
    }

    private void createTopRevenueDaysSheet(Workbook workbook, RevenueReportResponse report,
                                          CellStyle headerStyle, CellStyle currencyStyle,
                                          CellStyle numberStyle) {
        Sheet sheet = workbook.createSheet("Top 10 ngày");

        int rowNum = 0;

        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Thứ hạng");
        headerRow.createCell(1).setCellValue("Ngày");
        headerRow.createCell(2).setCellValue("Doanh thu");
        headerRow.createCell(3).setCellValue("Số đơn hàng");
        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.getCell(1).setCellStyle(headerStyle);
        headerRow.getCell(2).setCellStyle(headerStyle);
        headerRow.getCell(3).setCellStyle(headerStyle);

        // Data
        for (TopRevenueDayDTO dto : report.getTopRevenueDays()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getRank());
            row.createCell(1).setCellValue(dto.getDate().format(DATE_FORMATTER));
            
            Cell revenueCell = row.createCell(2);
            revenueCell.setCellValue(dto.getRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);
            
            Cell orderCell = row.createCell(3);
            orderCell.setCellValue(dto.getOrderCount());
            orderCell.setCellStyle(numberStyle);
        }

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
    }

    private void createRevenueBySellerSheet(Workbook workbook, RevenueReportResponse report,
                                           CellStyle headerStyle, CellStyle currencyStyle,
                                           CellStyle numberStyle) {
        Sheet sheet = workbook.createSheet("Doanh thu theo seller");

        int rowNum = 0;

        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Seller ID");
        headerRow.createCell(1).setCellValue("Tên shop");
        headerRow.createCell(2).setCellValue("Email");
        headerRow.createCell(3).setCellValue("Doanh thu");
        headerRow.createCell(4).setCellValue("Số đơn hàng");
        headerRow.createCell(5).setCellValue("Tiền COD");
        headerRow.createCell(6).setCellValue("Tiền Online");
        for (int i = 0; i < 7; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }

        // Data
        for (RevenueBySellerDTO dto : report.getRevenueBySeller()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getSellerId());
            row.createCell(1).setCellValue(dto.getSellerName());
            row.createCell(2).setCellValue(dto.getSellerEmail());
            
            Cell revenueCell = row.createCell(3);
            revenueCell.setCellValue(dto.getRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);
            
            Cell orderCell = row.createCell(4);
            orderCell.setCellValue(dto.getOrderCount());
            orderCell.setCellStyle(numberStyle);
            
            Cell codCell = row.createCell(5);
            codCell.setCellValue(dto.getCodAmount().doubleValue());
            codCell.setCellStyle(currencyStyle);
            
            Cell onlineCell = row.createCell(6);
            onlineCell.setCellValue(dto.getOnlineAmount().doubleValue());
            onlineCell.setCellStyle(currencyStyle);
        }

        // Auto-size columns
        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSummaryRow(Sheet sheet, int rowNum, String label, BigDecimal value, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value.doubleValue());
        valueCell.setCellStyle(valueStyle);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }
}





