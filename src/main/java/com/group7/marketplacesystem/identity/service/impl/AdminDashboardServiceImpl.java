package com.group7.marketplacesystem.identity.service.impl;

import com.group7.marketplacesystem.catalog.entity.Category;
import com.group7.marketplacesystem.catalog.repository.CategoryRepository;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.commerce.order.entity.Order;
import com.group7.marketplacesystem.commerce.order.entity.Orderdetail;
import com.group7.marketplacesystem.commerce.order.repository.OrderDetailRepository;
import com.group7.marketplacesystem.commerce.order.repository.OrderRepository;
import com.group7.marketplacesystem.commerce.payment.entity.Payment;
import com.group7.marketplacesystem.commerce.payment.repository.PaymentRepository;
import com.group7.marketplacesystem.identity.dto.request.RevenueFilterRequest;
import com.group7.marketplacesystem.identity.dto.response.*;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import com.group7.marketplacesystem.identity.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SellerRepository sellerRepository;
    private final PaymentRepository paymentRepository;
    
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.07"); // 7%

    @Override
    public DashboardKPIsResponse getKPIs() {
        // Revenue calculations
        BigDecimal revenueThisMonth = orderRepository.getRevenueThisMonth() != null ? 
            orderRepository.getRevenueThisMonth() : BigDecimal.ZERO;
        BigDecimal revenueThisYear = orderRepository.getRevenueThisYear() != null ? 
            orderRepository.getRevenueThisYear() : BigDecimal.ZERO;
        BigDecimal revenueLastMonth = orderRepository.getRevenueLastMonth() != null ? 
            orderRepository.getRevenueLastMonth() : BigDecimal.ZERO;
        
        Double revenueChangePercent = calculatePercentChange(revenueThisMonth, revenueLastMonth);

        // Order calculations
        Long ordersToday = orderRepository.countOrdersToday() != null ? 
            orderRepository.countOrdersToday() : 0L;
        Long ordersCompleted = orderRepository.countByOrderStatus("Delivered") != null ?
            orderRepository.countByOrderStatus("Delivered") : 0L;
        Long ordersProcessing = orderRepository.countByOrderStatus("Shipped") != null ?
            orderRepository.countByOrderStatus("Shipped") : 0L;
        ordersProcessing += orderRepository.countByOrderStatus("Pending") != null ?
            orderRepository.countByOrderStatus("Pending") : 0L;
        Long ordersCancelled = orderRepository.countByOrderStatus("Cancelled") != null ?
            orderRepository.countByOrderStatus("Cancelled") : 0L;

        // User calculations
        Long newUsersToday = userRepository.countNewUsersToday() != null ?
            userRepository.countNewUsersToday() : 0L;
        Long newBuyersToday = userRepository.countNewBuyersToday() != null ? 
            userRepository.countNewBuyersToday() : 0L;
        Long newSellersToday = userRepository.countNewSellersToday() != null ? 
            userRepository.countNewSellersToday() : 0L;
        Long activeUsers = userRepository.countActiveUsers() != null ? 
            userRepository.countActiveUsers() : 0L;
        Long bannedUsers = userRepository.countBannedUsers() != null ? 
            userRepository.countBannedUsers() : 0L;

        // Product calculations
        Long totalProducts = productRepository.countTotalProducts() != null ? 
            productRepository.countTotalProducts() : 0L;
        Long activeProducts = productRepository.countByProductStatus("Approved") != null ?
            productRepository.countByProductStatus("Approved") : 0L;
        Long outOfStockProducts = productRepository.countOutOfStockProducts() != null ? 
            productRepository.countOutOfStockProducts() : 0L;
        Long pendingProducts = productRepository.countByProductStatus("Pending") != null ? 
            productRepository.countByProductStatus("Pending") : 0L;

        // Seller calculations
        Long newSellersThisMonth = userRepository.countNewSellersThisMonth() != null ? 
            userRepository.countNewSellersThisMonth() : 0L;
        Long activeSellers = userRepository.countActiveSellers() != null ? 
            userRepository.countActiveSellers() : 0L;
        Long bannedSellers = userRepository.countBannedSellers() != null ? 
            userRepository.countBannedSellers() : 0L;

        // Calculate commission (7% of revenue)
        BigDecimal commissionThisMonth = revenueThisMonth.multiply(COMMISSION_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal commissionThisYear = revenueThisYear.multiply(COMMISSION_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal commissionLastMonth = revenueLastMonth.multiply(COMMISSION_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        Double commissionChangePercent = calculatePercentChange(commissionThisMonth, commissionLastMonth);
        
        return DashboardKPIsResponse.builder()
            .revenueThisMonth(revenueThisMonth)
            .revenueThisYear(revenueThisYear)
            .revenueChangePercent(revenueChangePercent)
            .commissionThisMonth(commissionThisMonth)
            .commissionThisYear(commissionThisYear)
            .commissionChangePercent(commissionChangePercent)
            .ordersToday(ordersToday)
            .ordersCompleted(ordersCompleted)
            .ordersProcessing(ordersProcessing)
            .ordersCancelled(ordersCancelled)
            .newUsersToday(newUsersToday)
            .newBuyersToday(newBuyersToday)
            .newSellersToday(newSellersToday)
            .activeUsers(activeUsers)
            .bannedUsers(bannedUsers)
            .totalProducts(totalProducts)
            .activeProducts(activeProducts)
            .outOfStockProducts(outOfStockProducts)
            .pendingProducts(pendingProducts)
            .newSellersThisMonth(newSellersThisMonth)
            .activeSellers(activeSellers)
            .bannedSellers(bannedSellers)
            .build();
    }

    @Override
    public RevenueChartDataResponse getRevenueChartData(String period) {
        List<RevenueChartDataResponse.RevenueDataPoint> dataPoints = new ArrayList<>();
        BigDecimal currentPeriodTotal = BigDecimal.ZERO;
        BigDecimal previousPeriodTotal = BigDecimal.ZERO;

        if ("daily".equals(period)) {
            // Get daily data for current month
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            int daysInMonth = now.lengthOfMonth();

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = startOfMonth.withDayOfMonth(day);
                Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                
                List<Order> orders = orderRepository.findByOrderDateBetween(startOfDay, endOfDay);
                BigDecimal dailyRevenue = orders.stream()
                    .filter(o -> o.getFinalAmount() != null)
                    .map(Order::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                currentPeriodTotal = currentPeriodTotal.add(dailyRevenue);
                
                dataPoints.add(RevenueChartDataResponse.RevenueDataPoint.builder()
                    .date(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .revenue(dailyRevenue)
                    .build());
            }

            // Get previous month total for comparison
            LocalDate lastMonth = now.minusMonths(1);
            Instant startLastMonth = lastMonth.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endLastMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).plusDays(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
            
            List<Order> lastMonthOrders = orderRepository.findByOrderDateBetween(startLastMonth, endLastMonth);
            previousPeriodTotal = lastMonthOrders.stream()
                .filter(o -> o.getFinalAmount() != null)
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            // Get monthly data for current year
            LocalDate now = LocalDate.now();
            int currentYear = now.getYear();

            for (int month = 1; month <= 12; month++) {
                LocalDate monthStart = LocalDate.of(currentYear, month, 1);
                LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
                Instant startInstant = monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant endInstant = monthEnd.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                
                List<Order> orders = orderRepository.findByOrderDateBetween(startInstant, endInstant);
                BigDecimal monthlyRevenue = orders.stream()
                    .filter(o -> o.getFinalAmount() != null)
                    .map(Order::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                currentPeriodTotal = currentPeriodTotal.add(monthlyRevenue);
                
                dataPoints.add(RevenueChartDataResponse.RevenueDataPoint.builder()
                    .date(String.format("%d-%02d", currentYear, month))
                    .revenue(monthlyRevenue)
                    .build());
            }

            // Get previous year total
            int lastYear = currentYear - 1;
            Instant startLastYear = LocalDate.of(lastYear, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endLastYear = LocalDate.of(lastYear, 12, 31).plusDays(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
            
            List<Order> lastYearOrders = orderRepository.findByOrderDateBetween(startLastYear, endLastYear);
            previousPeriodTotal = lastYearOrders.stream()
                .filter(o -> o.getFinalAmount() != null)
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        Double changePercent = calculatePercentChange(currentPeriodTotal, previousPeriodTotal);

        return RevenueChartDataResponse.builder()
            .dailyData("daily".equals(period) ? dataPoints : null)
            .monthlyData("monthly".equals(period) ? dataPoints : null)
            .currentPeriodTotal(currentPeriodTotal)
            .previousPeriodTotal(previousPeriodTotal)
            .changePercent(changePercent)
            .build();
    }

    @Override
    public OrderStatusChartResponse getOrderStatusChart() {
        List<Object[]> statusCounts = orderRepository.countByOrderStatusGrouped();
        List<OrderStatusChartResponse.OrderStatusData> statusData = statusCounts.stream()
            .map(result -> OrderStatusChartResponse.OrderStatusData.builder()
                .status((String) result[0])
                .count(((Number) result[1]).longValue())
                .build())
            .collect(Collectors.toList());

        return OrderStatusChartResponse.builder()
            .statusData(statusData)
            .build();
    }

    @Override
    public UserGrowthChartResponse getUserGrowthChart() {
        List<Object[]> buyerData = userRepository.countUsersByMonth("BUYER");
        List<Object[]> sellerData = userRepository.countUsersByMonth("SELLER");

        List<UserGrowthChartResponse.UserGrowthDataPoint> buyerPoints = buyerData.stream()
            .map(result -> UserGrowthChartResponse.UserGrowthDataPoint.builder()
                .month((String) result[0])
                .count(((Number) result[1]).longValue())
                .build())
            .collect(Collectors.toList());

        List<UserGrowthChartResponse.UserGrowthDataPoint> sellerPoints = sellerData.stream()
            .map(result -> UserGrowthChartResponse.UserGrowthDataPoint.builder()
                .month((String) result[0])
                .count(((Number) result[1]).longValue())
                .build())
            .collect(Collectors.toList());

        return UserGrowthChartResponse.builder()
            .buyerData(buyerPoints)
            .sellerData(sellerPoints)
            .build();
    }

    @Override
    public TopSellersProductsResponse getTopSellersAndProducts(int limit) {
        // Get all completed orders
        List<Order> completedOrders = orderRepository.findAll().stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()) && o.getFinalAmount() != null)
            .collect(Collectors.toList());

        // Calculate seller revenue
        Map<Integer, SellerRevenue> sellerRevenueMap = new HashMap<>();
        for (Order order : completedOrders) {
            Integer sellerId = order.getSeller().getId();
            SellerRevenue sellerRevenue = sellerRevenueMap.getOrDefault(sellerId, 
                new SellerRevenue(sellerId, order.getSeller().getShopName(), BigDecimal.ZERO, 0L));
            sellerRevenue.revenue = sellerRevenue.revenue.add(order.getFinalAmount());
            sellerRevenue.orderCount++;
            sellerRevenueMap.put(sellerId, sellerRevenue);
        }

        List<TopSellersProductsResponse.TopSellerData> topSellers = sellerRevenueMap.values().stream()
            .sorted((a, b) -> b.revenue.compareTo(a.revenue))
            .limit(limit)
            .map(sr -> TopSellersProductsResponse.TopSellerData.builder()
                .sellerId(sr.sellerId)
                .shopName(sr.shopName)
                .revenue(sr.revenue)
                .orderCount(sr.orderCount)
                .build())
            .collect(Collectors.toList());

        // Calculate product sales
        Map<Integer, ProductSales> productSalesMap = new HashMap<>();
        for (Order order : completedOrders) {
            List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
            for (Orderdetail detail : orderDetails) {
                Integer productId = detail.getProduct().getId();
                ProductSales productSales = productSalesMap.getOrDefault(productId,
                    new ProductSales(productId, detail.getProduct().getName(), 0L, BigDecimal.ZERO));
                productSales.salesCount += detail.getQuantity();
                productSales.revenue = productSales.revenue.add(
                    detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
                productSalesMap.put(productId, productSales);
            }
        }

        List<TopSellersProductsResponse.TopProductData> topProducts = productSalesMap.values().stream()
            .sorted((a, b) -> Long.compare(b.salesCount, a.salesCount))
            .limit(limit)
            .map(ps -> TopSellersProductsResponse.TopProductData.builder()
                .productId(ps.productId)
                .productName(ps.productName)
                .salesCount(ps.salesCount)
                .revenue(ps.revenue)
                .build())
            .collect(Collectors.toList());

        return TopSellersProductsResponse.builder()
            .topSellers(topSellers)
            .topProducts(topProducts)
            .build();
    }

    @Override
    public CategoryDistributionResponse getCategoryDistribution() {
        // Product count by category
        List<Object[]> productCounts = productRepository.countByCategory();
        List<CategoryDistributionResponse.CategoryData> productCountData = new ArrayList<>();
        
        for (Object[] result : productCounts) {
            Integer categoryId = (Integer) result[0];
            Long count = ((Number) result[1]).longValue();
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                productCountData.add(CategoryDistributionResponse.CategoryData.builder()
                    .categoryId(categoryId)
                    .categoryName(category.getName())
                    .count(count)
                    .build());
            }
        }

        // Revenue by category from completed orders
        Map<Integer, CategoryRevenue> categoryRevenueMap = new HashMap<>();
        List<Order> completedOrders = orderRepository.findAll().stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .collect(Collectors.toList());

        for (Order order : completedOrders) {
            List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
            for (Orderdetail detail : orderDetails) {
                Integer categoryId = detail.getProduct().getCategory().getId();
                CategoryRevenue categoryRevenue = categoryRevenueMap.getOrDefault(categoryId,
                    new CategoryRevenue(categoryId, detail.getProduct().getCategory().getName(), BigDecimal.ZERO));
                categoryRevenue.revenue = categoryRevenue.revenue.add(
                    detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
                categoryRevenueMap.put(categoryId, categoryRevenue);
            }
        }

        List<CategoryDistributionResponse.CategoryData> revenueData = categoryRevenueMap.values().stream()
            .map(cr -> CategoryDistributionResponse.CategoryData.builder()
                .categoryId(cr.categoryId)
                .categoryName(cr.categoryName)
                .revenue(cr.revenue)
                .build())
            .collect(Collectors.toList());

        return CategoryDistributionResponse.builder()
            .productCountByCategory(productCountData)
            .revenueByCategory(revenueData)
            .build();
    }

    private Double calculatePercentChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        BigDecimal change = current.subtract(previous);
        BigDecimal percentChange = change.divide(previous, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        return percentChange.doubleValue();
    }

    // Helper classes
    private static class SellerRevenue {
        Integer sellerId;
        String shopName;
        BigDecimal revenue;
        Long orderCount;

        SellerRevenue(Integer sellerId, String shopName, BigDecimal revenue, Long orderCount) {
            this.sellerId = sellerId;
            this.shopName = shopName;
            this.revenue = revenue;
            this.orderCount = orderCount;
        }
    }

    private static class ProductSales {
        Integer productId;
        String productName;
        Long salesCount;
        BigDecimal revenue;

        ProductSales(Integer productId, String productName, Long salesCount, BigDecimal revenue) {
            this.productId = productId;
            this.productName = productName;
            this.salesCount = salesCount;
            this.revenue = revenue;
        }
    }

    private static class CategoryRevenue {
        Integer categoryId;
        String categoryName;
        BigDecimal revenue;

        CategoryRevenue(Integer categoryId, String categoryName, BigDecimal revenue) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.revenue = revenue;
        }
    }
    
    @Override
    public AdminRevenueResponse getAdminRevenue(RevenueFilterRequest filter) {
        // Set default values
        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : 
            LocalDate.now().minusMonths(1);
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : 
            LocalDate.now().plusDays(1);
        String periodType = filter.getPeriodType() != null ? filter.getPeriodType() : "daily";
        Integer topLimit = filter.getTopSellersLimit() != null ? filter.getTopSellersLimit() : 10;
        
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        // Get total revenue and orders
        BigDecimal totalRevenue = orderRepository.getTotalRevenue(startInstant, endInstant, filter.getSellerId());
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        Long totalOrders = orderRepository.getTotalOrders(startInstant, endInstant, filter.getSellerId());
        if (totalOrders == null) totalOrders = 0L;
        
        // Calculate commission (7%)
        BigDecimal commission = totalRevenue.multiply(COMMISSION_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        
        // Calculate average order value
        BigDecimal averageOrderValue = totalOrders > 0 ? 
            totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        // Get revenue by period
        List<AdminRevenueResponse.RevenueDataPoint> revenueByPeriod = new ArrayList<>();
        List<Object[]> periodData;
        
        switch (periodType.toLowerCase()) {
            case "monthly":
                periodData = orderRepository.getRevenueByMonth(startInstant, endInstant, filter.getSellerId());
                break;
            case "quarterly":
                periodData = orderRepository.getRevenueByQuarter(startInstant, endInstant, filter.getSellerId());
                break;
            case "yearly":
                periodData = orderRepository.getRevenueByYear(startInstant, endInstant, filter.getSellerId());
                break;
            default: // daily
                periodData = orderRepository.getRevenueByDay(startInstant, endInstant, filter.getSellerId());
        }
        
        for (Object[] row : periodData) {
            if (row.length < 3) continue;
            
            Object periodObj = row[0];
            String period;
            if (periodObj == null) {
                period = "";
            } else if (periodObj instanceof Number) {
                // For yearly, period might be a number
                period = String.valueOf(periodObj);
            } else {
                period = periodObj.toString();
            }
            
            Number revenueNum = (Number) row[1];
            BigDecimal revenue = revenueNum != null ? 
                BigDecimal.valueOf(revenueNum.doubleValue()) : BigDecimal.ZERO;
            Number orderCountNum = (Number) row[2];
            Long orderCount = orderCountNum != null ? orderCountNum.longValue() : 0L;
            BigDecimal periodCommission = revenue.multiply(COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);
            
            revenueByPeriod.add(AdminRevenueResponse.RevenueDataPoint.builder()
                .period(period)
                .revenue(revenue)
                .commission(periodCommission)
                .orderCount(orderCount)
                .build());
        }
        
        // Get top sellers
        List<AdminRevenueResponse.TopSellerRevenue> topSellers = new ArrayList<>();
        List<Object[]> sellerData = orderRepository.getRevenueBySeller(startInstant, endInstant);
        
        // Filter by category if needed
        if (filter.getCategoryId() != null) {
            sellerData = filterRevenueByCategory(sellerData, filter.getCategoryId(), startInstant, endInstant);
        }
        
        // Sort and limit
        sellerData = sellerData.stream()
            .sorted((a, b) -> {
                BigDecimal revenueA = BigDecimal.valueOf(((Number) a[1]).doubleValue());
                BigDecimal revenueB = BigDecimal.valueOf(((Number) b[1]).doubleValue());
                return revenueB.compareTo(revenueA);
            })
            .limit(topLimit)
            .collect(Collectors.toList());
        
        for (Object[] row : sellerData) {
            if (row.length < 3) continue;
            
            Number sellerIdNum = (Number) row[0];
            Integer sellerId = sellerIdNum != null ? sellerIdNum.intValue() : null;
            if (sellerId == null) continue;
            
            Number revenueNum = (Number) row[1];
            BigDecimal sellerRevenue = revenueNum != null ? 
                BigDecimal.valueOf(revenueNum.doubleValue()) : BigDecimal.ZERO;
            
            Number orderCountNum = (Number) row[2];
            Long sellerOrderCount = orderCountNum != null ? orderCountNum.longValue() : 0L;
            
            BigDecimal sellerCommission = sellerRevenue.multiply(COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);
            
            Seller seller = sellerRepository.findById(sellerId).orElse(null);
            String shopName = seller != null && seller.getShopName() != null ? 
                seller.getShopName() : "N/A";
            
            topSellers.add(AdminRevenueResponse.TopSellerRevenue.builder()
                .sellerId(sellerId)
                .shopName(shopName)
                .revenue(sellerRevenue)
                .commission(sellerCommission)
                .orderCount(sellerOrderCount)
                .build());
        }
        
        // Get revenue by category
        List<AdminRevenueResponse.CategoryRevenue> revenueByCategory = new ArrayList<>();
        Map<Integer, CategoryRevenueData> categoryMap = new HashMap<>();
        
        List<Order> deliveredOrders = orderRepository.findAll().stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .filter(o -> {
                Instant orderDate = o.getOrderDate();
                return orderDate != null && 
                       !orderDate.isBefore(startInstant) && 
                       orderDate.isBefore(endInstant);
            })
            .filter(o -> filter.getSellerId() == null || o.getSeller().getId().equals(filter.getSellerId()))
            .collect(Collectors.toList());
        
        for (Order order : deliveredOrders) {
            List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
            for (Orderdetail detail : orderDetails) {
                Integer categoryId = detail.getProduct().getCategory().getId();
                
                if (filter.getCategoryId() == null || categoryId.equals(filter.getCategoryId())) {
                    CategoryRevenueData catData = categoryMap.getOrDefault(categoryId,
                        new CategoryRevenueData(categoryId, detail.getProduct().getCategory().getName(), 
                            BigDecimal.ZERO, 0L));
                    catData.revenue = catData.revenue.add(
                        detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
                    catData.orderCount++;
                    categoryMap.put(categoryId, catData);
                }
            }
        }
        
        for (CategoryRevenueData catData : categoryMap.values()) {
            BigDecimal catCommission = catData.revenue.multiply(COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);
            
            revenueByCategory.add(AdminRevenueResponse.CategoryRevenue.builder()
                .categoryId(catData.categoryId)
                .categoryName(catData.categoryName)
                .revenue(catData.revenue)
                .commission(catCommission)
                .orderCount(catData.orderCount)
                .build());
        }
        
        revenueByCategory.sort((a, b) -> b.getRevenue().compareTo(a.getRevenue()));
        
        // Seller stats if filtered by seller
        AdminRevenueResponse.SellerRevenueStats sellerStats = null;
        if (filter.getSellerId() != null) {
            Seller seller = sellerRepository.findById(filter.getSellerId()).orElse(null);
            if (seller != null) {
                sellerStats = AdminRevenueResponse.SellerRevenueStats.builder()
                    .sellerId(filter.getSellerId())
                    .shopName(seller.getShopName() != null ? seller.getShopName() : "N/A")
                    .totalRevenue(totalRevenue)
                    .totalCommission(commission)
                    .totalOrders(totalOrders)
                    .averageOrderValue(averageOrderValue)
                    .build();
            }
        }
        
        // Calculate Service Package Revenue
        BigDecimal totalServicePackageRevenue = paymentRepository.getTotalServicePackageRevenue(
            startInstant, endInstant, filter.getSellerId());
        if (totalServicePackageRevenue == null) totalServicePackageRevenue = BigDecimal.ZERO;
        
        Long totalServicePackages = paymentRepository.getTotalServicePackageCount(
            startInstant, endInstant, filter.getSellerId());
        if (totalServicePackages == null) totalServicePackages = 0L;
        
        // Get service package revenue by period
        List<AdminRevenueResponse.ServicePackageRevenueDataPoint> servicePackageRevenueByPeriod = new ArrayList<>();
        String dateFormat;
        switch (periodType.toLowerCase()) {
            case "monthly":
                dateFormat = "%Y-%m";
                break;
            case "quarterly":
                dateFormat = "%Y-Q%q";
                break;
            case "yearly":
                dateFormat = "%Y";
                break;
            default: // daily
                dateFormat = "%Y-%m-%d";
        }
        
        List<Object[]> servicePackagePeriodData = paymentRepository.getServicePackageRevenueByPeriod(
            startInstant, endInstant, filter.getSellerId(), dateFormat);
        
        for (Object[] row : servicePackagePeriodData) {
            if (row.length < 3) continue;
            
            String period = row[0] != null ? row[0].toString() : "";
            Number revenueNum = (Number) row[1];
            BigDecimal revenue = revenueNum != null ? 
                BigDecimal.valueOf(revenueNum.doubleValue()) : BigDecimal.ZERO;
            Number packageCountNum = (Number) row[2];
            Long packageCount = packageCountNum != null ? packageCountNum.longValue() : 0L;
            
            servicePackageRevenueByPeriod.add(AdminRevenueResponse.ServicePackageRevenueDataPoint.builder()
                .period(period)
                .revenue(revenue)
                .packageCount(packageCount)
                .build());
        }
        
        // Get top sellers by service package revenue
        List<AdminRevenueResponse.TopSellerServicePackageRevenue> topSellersByServicePackage = new ArrayList<>();
        List<Object[]> sellerPackageData = paymentRepository.getServicePackageRevenueBySeller(
            startInstant, endInstant);
        
        // Filter by seller if needed
        if (filter.getSellerId() != null) {
            sellerPackageData = sellerPackageData.stream()
                .filter(row -> {
                    Number sellerIdNum = (Number) row[0];
                    return sellerIdNum != null && sellerIdNum.intValue() == filter.getSellerId();
                })
                .collect(Collectors.toList());
        }
        
        // Limit to top sellers
        sellerPackageData = sellerPackageData.stream()
            .sorted((a, b) -> {
                BigDecimal revenueA = BigDecimal.valueOf(((Number) a[1]).doubleValue());
                BigDecimal revenueB = BigDecimal.valueOf(((Number) b[1]).doubleValue());
                return revenueB.compareTo(revenueA);
            })
            .limit(topLimit)
            .collect(Collectors.toList());
        
        for (Object[] row : sellerPackageData) {
            if (row.length < 3) continue;
            
            Number sellerIdNum = (Number) row[0];
            Integer sellerId = sellerIdNum != null ? sellerIdNum.intValue() : null;
            if (sellerId == null) continue;
            
            Number revenueNum = (Number) row[1];
            BigDecimal sellerRevenue = revenueNum != null ? 
                BigDecimal.valueOf(revenueNum.doubleValue()) : BigDecimal.ZERO;
            
            Number packageCountNum = (Number) row[2];
            Long packageCount = packageCountNum != null ? packageCountNum.longValue() : 0L;
            
            Seller seller = sellerRepository.findById(sellerId).orElse(null);
            String shopName = seller != null && seller.getShopName() != null ? 
                seller.getShopName() : "N/A";
            
            topSellersByServicePackage.add(AdminRevenueResponse.TopSellerServicePackageRevenue.builder()
                .sellerId(sellerId)
                .shopName(shopName)
                .revenue(sellerRevenue)
                .packageCount(packageCount)
                .build());
        }
        
        return AdminRevenueResponse.builder()
            .totalRevenue(totalRevenue)
            .commission(commission)
            .totalOrders(totalOrders)
            .averageOrderValue(averageOrderValue)
            .revenueByPeriod(revenueByPeriod)
            .topSellers(topSellers)
            .revenueByCategory(revenueByCategory)
            .sellerStats(sellerStats)
            .totalServicePackageRevenue(totalServicePackageRevenue)
            .totalServicePackages(totalServicePackages)
            .servicePackageRevenueByPeriod(servicePackageRevenueByPeriod)
            .topSellersByServicePackage(topSellersByServicePackage)
            .build();
    }
    
    private List<Object[]> filterRevenueByCategory(List<Object[]> sellerData, Integer categoryId, 
                                                   Instant startDate, Instant endDate) {
        // Filter sellers that have orders with products in this category
        Set<Integer> validSellerIds = new HashSet<>();
        
        List<Order> orders = orderRepository.findAll().stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .filter(o -> {
                Instant orderDate = o.getOrderDate();
                return orderDate != null && 
                       !orderDate.isBefore(startDate) && 
                       orderDate.isBefore(endDate);
            })
            .collect(Collectors.toList());
        
        for (Order order : orders) {
            List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
            for (Orderdetail detail : orderDetails) {
                if (detail.getProduct().getCategory().getId().equals(categoryId)) {
                    validSellerIds.add(order.getSeller().getId());
                    break;
                }
            }
        }
        
        return sellerData.stream()
            .filter(row -> validSellerIds.contains(((Number) row[0]).intValue()))
            .collect(Collectors.toList());
    }
    
    private static class CategoryRevenueData {
        Integer categoryId;
        String categoryName;
        BigDecimal revenue;
        Long orderCount;
        
        CategoryRevenueData(Integer categoryId, String categoryName, BigDecimal revenue, Long orderCount) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.revenue = revenue;
            this.orderCount = orderCount;
        }
    }
    
    @Override
    public List<OrderDetailRevenueResponse> getOrderDetails(RevenueFilterRequest filter) {
        // Set default values
        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : 
            LocalDate.now().minusMonths(1);
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : 
            LocalDate.now().plusDays(1);
        
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        // Get orders with filters
        List<Order> orders = orderRepository.findAll().stream()
            .filter(o -> "Delivered".equals(o.getOrderStatus()))
            .filter(o -> {
                Instant orderDate = o.getOrderDate();
                return orderDate != null && 
                       !orderDate.isBefore(startInstant) && 
                       orderDate.isBefore(endInstant);
            })
            .filter(o -> filter.getSellerId() == null || o.getSeller().getId().equals(filter.getSellerId()))
            .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate())) // Latest first
            .collect(Collectors.toList());
        
        // Filter by category if needed
        if (filter.getCategoryId() != null) {
            Set<Integer> validOrderIds = new HashSet<>();
            for (Order order : orders) {
                List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
                for (Orderdetail detail : orderDetails) {
                    if (detail.getProduct().getCategory().getId().equals(filter.getCategoryId())) {
                        validOrderIds.add(order.getId());
                        break;
                    }
                }
            }
            orders = orders.stream()
                .filter(o -> validOrderIds.contains(o.getId()))
                .collect(Collectors.toList());
        }
        
        List<OrderDetailRevenueResponse> result = new ArrayList<>();
        
        for (Order order : orders) {
            // Get seller info
            Seller seller = order.getSeller();
            User sellerUser = seller.getUsers();
            String shopName = seller.getShopName() != null ? seller.getShopName() : "N/A";
            String sellerEmail = sellerUser != null && sellerUser.getEmail() != null ? 
                sellerUser.getEmail() : "N/A";
            
            // Get buyer info
            com.group7.marketplacesystem.identity.entity.Buyer buyer = order.getBuyer();
            User buyerUser = buyer.getUsers();
            String buyerName = buyerUser != null && buyerUser.getFullName() != null ? 
                buyerUser.getFullName() : "N/A";
            String buyerEmail = buyerUser != null && buyerUser.getEmail() != null ? 
                buyerUser.getEmail() : "N/A";
            
            // Calculate commission
            BigDecimal finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() : BigDecimal.ZERO;
            BigDecimal commission = finalAmount.multiply(COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);
            
            // Get order items
            List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
            List<OrderDetailRevenueResponse.OrderItemDetail> items = new ArrayList<>();
            
            for (Orderdetail detail : orderDetails) {
                // Filter by category if needed
                if (filter.getCategoryId() != null && 
                    !detail.getProduct().getCategory().getId().equals(filter.getCategoryId())) {
                    continue;
                }
                
                BigDecimal subtotal = detail.getUnitPrice()
                    .multiply(BigDecimal.valueOf(detail.getQuantity()));
                BigDecimal itemCommission = subtotal.multiply(COMMISSION_RATE)
                    .setScale(2, RoundingMode.HALF_UP);
                
                items.add(OrderDetailRevenueResponse.OrderItemDetail.builder()
                    .productId(detail.getProduct().getId())
                    .productName(detail.getProduct().getName())
                    .categoryName(detail.getProduct().getCategory().getName())
                    .quantity(detail.getQuantity())
                    .unitPrice(detail.getUnitPrice())
                    .subtotal(subtotal)
                    .itemCommission(itemCommission)
                    .build());
            }
            
            // Get payment info
            Optional<Payment> paymentOpt = paymentRepository.findByTargetIdAndTargetType(
                order.getId(), "Order");
            String paymentMethod = "N/A";
            String paymentStatus = "N/A";
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                paymentMethod = payment.getMethod() != null ? payment.getMethod() : "N/A";
                paymentStatus = payment.getStatus() != null ? payment.getStatus() : "N/A";
            }
            
            result.add(OrderDetailRevenueResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .sellerId(seller.getId())
                .shopName(shopName)
                .sellerEmail(sellerEmail)
                .buyerId(buyer.getId())
                .buyerName(buyerName)
                .buyerEmail(buyerEmail)
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO)
                .shippingFee(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO)
                .finalAmount(finalAmount)
                .commission(commission)
                .items(items)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .build());
        }
        
        return result;
    }
}

