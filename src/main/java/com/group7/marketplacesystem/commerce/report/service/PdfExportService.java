package com.group7.marketplacesystem.commerce.report.service;

import com.group7.marketplacesystem.commerce.report.dto.response.RevenueReportResponse;

import java.io.ByteArrayOutputStream;

public interface PdfExportService {
    ByteArrayOutputStream generatePdfReport(RevenueReportResponse report);
}

