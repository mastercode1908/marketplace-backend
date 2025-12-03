package com.group7.marketplacesystem.commerce.report.service;

import com.group7.marketplacesystem.commerce.report.dto.response.RevenueReportResponse;
import org.apache.poi.ss.usermodel.Workbook;

public interface ExcelExportService {
    Workbook generateExcelReport(RevenueReportResponse report);
}





