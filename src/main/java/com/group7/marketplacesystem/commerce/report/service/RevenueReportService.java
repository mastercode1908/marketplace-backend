package com.group7.marketplacesystem.commerce.report.service;

import com.group7.marketplacesystem.commerce.report.dto.request.RevenueReportRequest;
import com.group7.marketplacesystem.commerce.report.dto.response.RevenueReportResponse;

public interface RevenueReportService {
    RevenueReportResponse generateReport(RevenueReportRequest request);
}





