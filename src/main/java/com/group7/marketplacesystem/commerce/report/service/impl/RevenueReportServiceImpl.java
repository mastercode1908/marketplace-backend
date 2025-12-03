package com.group7.marketplacesystem.commerce.report.service.impl;

import com.group7.marketplacesystem.commerce.order.repository.OrderRepository;
import com.group7.marketplacesystem.commerce.payment.repository.PaymentRepository;
import com.group7.marketplacesystem.commerce.report.dto.request.RevenueReportRequest;
import com.group7.marketplacesystem.commerce.report.dto.response.RevenueByPeriodDTO;
import com.group7.marketplacesystem.commerce.report.dto.response.RevenueBySellerDTO;
import com.group7.marketplacesystem.commerce.report.dto.response.RevenueReportResponse;
import com.group7.marketplacesystem.commerce.report.dto.response.TopRevenueDayDTO;
import com.group7.marketplacesystem.commerce.report.service.RevenueReportService;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevenueReportServiceImpl implements RevenueReportService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SellerRepository sellerRepository;

    @Override
    public RevenueReportResponse generateReport(RevenueReportRequest request) {
        // Set default dates if not provided
        LocalDate startDate = request.getStartDate() != null 
            ? request.getStartDate() 
            : LocalDate.now().minusMonths(1);
        LocalDate endDate = request.getEndDate() != null 
            ? request.getEndDate() 
            : LocalDate.now().plusDays(1);
        
        String periodType = request.getPeriodType() != null 
            ? request.getPeriodType() 
            : "daily";
        
        Integer sellerId = request.getSellerId();

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Get totals
        BigDecimal totalRevenue = orderRepository.getTotalRevenue(startInstant, endInstant, sellerId);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        Long totalOrders = orderRepository.getTotalOrders(startInstant, endInstant, sellerId);
        if (totalOrders == null) totalOrders = 0L;

        BigDecimal totalCodAmount = paymentRepository.getTotalCodAmount(startInstant, endInstant, sellerId);
        if (totalCodAmount == null) totalCodAmount = BigDecimal.ZERO;

        BigDecimal totalOnlineAmount = paymentRepository.getTotalOnlineAmount(startInstant, endInstant, sellerId);
        if (totalOnlineAmount == null) totalOnlineAmount = BigDecimal.ZERO;

        // Get revenue by period
        List<RevenueByPeriodDTO> revenueByPeriod = getRevenueByPeriod(
            startInstant, endInstant, periodType, sellerId);

        // Get top 10 revenue days
        List<TopRevenueDayDTO> topRevenueDays = getTopRevenueDays(
            startInstant, endInstant, sellerId);

        // Get revenue by seller (only if sellerId is null)
        List<RevenueBySellerDTO> revenueBySeller = new ArrayList<>();
        if (sellerId == null) {
            revenueBySeller = getRevenueBySeller(startInstant, endInstant);
        }

        return RevenueReportResponse.builder()
            .startDate(startDate)
            .endDate(endDate.minusDays(1))
            .periodType(periodType)
            .totalRevenue(totalRevenue)
            .totalOrders(totalOrders)
            .totalCodAmount(totalCodAmount)
            .totalOnlineAmount(totalOnlineAmount)
            .revenueByPeriod(revenueByPeriod)
            .topRevenueDays(topRevenueDays)
            .revenueBySeller(revenueBySeller)
            .build();
    }

    private List<RevenueByPeriodDTO> getRevenueByPeriod(
            Instant startDate, Instant endDate, String periodType, Integer sellerId) {
        List<Object[]> results;
        
        switch (periodType.toLowerCase()) {
            case "monthly":
                results = orderRepository.getRevenueByMonth(startDate, endDate, sellerId);
                return results.stream()
                    .map(row -> RevenueByPeriodDTO.builder()
                        .periodLabel(formatMonthLabel((String) row[0]))
                        .revenue((BigDecimal) row[1])
                        .orderCount(((Number) row[2]).longValue())
                        .build())
                    .collect(Collectors.toList());
            
            case "quarterly":
                results = orderRepository.getRevenueByQuarter(startDate, endDate, sellerId);
                return results.stream()
                    .map(row -> RevenueByPeriodDTO.builder()
                        .periodLabel((String) row[0])
                        .revenue((BigDecimal) row[1])
                        .orderCount(((Number) row[2]).longValue())
                        .build())
                    .collect(Collectors.toList());
            
            case "yearly":
                results = orderRepository.getRevenueByYear(startDate, endDate, sellerId);
                return results.stream()
                    .map(row -> RevenueByPeriodDTO.builder()
                        .periodLabel("Năm " + row[0])
                        .revenue((BigDecimal) row[1])
                        .orderCount(((Number) row[2]).longValue())
                        .build())
                    .collect(Collectors.toList());
            
            case "daily":
            default:
                results = orderRepository.getRevenueByDay(startDate, endDate, sellerId);
                return results.stream()
                    .map(row -> {
                        LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                        return RevenueByPeriodDTO.builder()
                            .period(date)
                            .periodLabel(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                            .revenue((BigDecimal) row[1])
                            .orderCount(((Number) row[2]).longValue())
                            .build();
                    })
                    .collect(Collectors.toList());
        }
    }

    private List<TopRevenueDayDTO> getTopRevenueDays(
            Instant startDate, Instant endDate, Integer sellerId) {
        List<Object[]> results = orderRepository.getTopRevenueDays(startDate, endDate, sellerId);
        
        List<TopRevenueDayDTO> topDays = new ArrayList<>();
        int rank = 1;
        for (Object[] row : results) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            topDays.add(TopRevenueDayDTO.builder()
                .date(date)
                .revenue((BigDecimal) row[1])
                .orderCount(((Number) row[2]).longValue())
                .rank(rank++)
                .build());
        }
        return topDays;
    }

    private List<RevenueBySellerDTO> getRevenueBySeller(Instant startDate, Instant endDate) {
        List<Object[]> orderResults = orderRepository.getRevenueBySeller(startDate, endDate);
        List<Object[]> paymentResults = paymentRepository.getPaymentAmountsBySeller(startDate, endDate);

        // Create map for payment amounts by seller
        Map<Integer, Object[]> paymentMap = paymentResults.stream()
            .collect(Collectors.toMap(
                row -> (Integer) row[0],
                row -> row
            ));

        List<RevenueBySellerDTO> revenueBySeller = new ArrayList<>();
        for (Object[] row : orderResults) {
            Integer sellerId = (Integer) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            Long orderCount = ((Number) row[2]).longValue();

            Seller seller = sellerRepository.findById(sellerId).orElse(null);
            String sellerName = seller != null && seller.getShopName() != null 
                ? seller.getShopName() 
                : "N/A";
            String sellerEmail = seller != null && seller.getUsers() != null 
                ? seller.getUsers().getEmail() 
                : "N/A";

            BigDecimal codAmount = BigDecimal.ZERO;
            BigDecimal onlineAmount = BigDecimal.ZERO;
            if (paymentMap.containsKey(sellerId)) {
                Object[] paymentRow = paymentMap.get(sellerId);
                codAmount = (BigDecimal) paymentRow[1];
                onlineAmount = (BigDecimal) paymentRow[2];
            }

            revenueBySeller.add(RevenueBySellerDTO.builder()
                .sellerId(sellerId)
                .sellerName(sellerName)
                .sellerEmail(sellerEmail)
                .revenue(revenue)
                .orderCount(orderCount)
                .codAmount(codAmount)
                .onlineAmount(onlineAmount)
                .build());
        }

        return revenueBySeller;
    }

    private String formatMonthLabel(String period) {
        // period format: "2024-01"
        String[] parts = period.split("-");
        if (parts.length == 2) {
            return "Tháng " + Integer.parseInt(parts[1]) + "/" + parts[0];
        }
        return period;
    }
}





