package com.group7.marketplacesystem.commerce.order.service.impl;

import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.commerce.order.entity.Delivery;
import com.group7.marketplacesystem.commerce.order.entity.Order;
import com.group7.marketplacesystem.commerce.order.entity.Orderdetail;
import com.group7.marketplacesystem.commerce.order.mapper.OrderMapper;
import com.group7.marketplacesystem.commerce.order.repository.DeliveryRepository;
import com.group7.marketplacesystem.commerce.order.repository.OrderDetailRepository;
import com.group7.marketplacesystem.commerce.order.repository.OrderRepository;
import com.group7.marketplacesystem.commerce.order.service.OrderService;
import com.group7.marketplacesystem.commerce.shipping.dto.ghn.GHNOrderDetailData;
import com.group7.marketplacesystem.commerce.shipping.dto.response.BuyerAddressResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.OrderDetailResponse;
import com.group7.marketplacesystem.commerce.shipping.entity.GHNShopInfo;
import com.group7.marketplacesystem.commerce.shipping.repository.GHNShopInfoRepository;
import com.group7.marketplacesystem.commerce.shipping.service.GHNService;
import com.group7.marketplacesystem.commerce.shipping.service.ShippingService;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.infrastructure.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final DeliveryRepository deliveryRepository;
    private final ShippingService shippingService;
    private final GHNService ghnService;
    private final GHNShopInfoRepository ghnShopInfoRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final MailService mailService;

    @Override
    @Transactional
    public List<OrderDetailResponse> getOrdersByBuyerId(Integer buyerId) {
        List<Order> orders = orderRepository.findByBuyerId(buyerId);
        
        // Tự động đồng bộ trạng thái từ GHN cho các đơn hàng chưa hoàn thành
        for (Order order : orders) {
            syncOrderIfNeeded(order);
        }
        
        // Reload orders sau khi sync để lấy dữ liệu mới nhất
        // Không cần flush vì @Transactional sẽ tự động commit khi method kết thúc
        orders = orderRepository.findByBuyerId(buyerId);
        
        return orders.stream()
                .map(order -> {
                    Delivery delivery = deliveryRepository.findByOrderId(order.getId()).orElse(null);
                    List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
                    return orderMapper.toOrderDetailResponse(order, null, delivery, orderDetails);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<OrderDetailResponse> getOrdersBySellerId(Integer sellerId) {
        List<Order> orders = orderRepository.findBySellerId(sellerId);
        
        // Tự động đồng bộ trạng thái từ GHN cho các đơn hàng chưa hoàn thành
        for (Order order : orders) {
            syncOrderIfNeeded(order);
        }
        
        // Reload orders sau khi sync để lấy dữ liệu mới nhất
        // Không cần flush vì @Transactional sẽ tự động commit khi method kết thúc
        orders = orderRepository.findBySellerId(sellerId);
        
        return orders.stream()
                .map(order -> {
                    Delivery delivery = deliveryRepository.findByOrderId(order.getId()).orElse(null);
                    List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
                    return orderMapper.toOrderDetailResponse(order, null, delivery, orderDetails);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Page<OrderDetailResponse> getOrdersByBuyerId(Integer buyerId, Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findByBuyerIdOrderByOrderDateDesc(buyerId, pageable);
        
        // Tự động đồng bộ trạng thái từ GHN cho các đơn hàng chưa hoàn thành
        for (Order order : ordersPage.getContent()) {
            syncOrderIfNeeded(order);
        }
        
        // Reload orders sau khi sync để lấy dữ liệu mới nhất
        ordersPage = orderRepository.findByBuyerIdOrderByOrderDateDesc(buyerId, pageable);
        
        return ordersPage.map(order -> {
            Delivery delivery = deliveryRepository.findByOrderId(order.getId()).orElse(null);
            List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
            return orderMapper.toOrderDetailResponse(order, null, delivery, orderDetails);
        });
    }

    @Override
    @Transactional
    public Page<OrderDetailResponse> getOrdersBySellerId(Integer sellerId, Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId, pageable);
        
        // Tự động đồng bộ trạng thái từ GHN cho các đơn hàng chưa hoàn thành
        for (Order order : ordersPage.getContent()) {
            syncOrderIfNeeded(order);
        }
        
        // Reload orders sau khi sync để lấy dữ liệu mới nhất
        ordersPage = orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId, pageable);
        
        return ordersPage.map(order -> {
            Delivery delivery = deliveryRepository.findByOrderId(order.getId()).orElse(null);
            List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
            return orderMapper.toOrderDetailResponse(order, null, delivery, orderDetails);
        });
    }

    /**
     * Đồng bộ trạng thái từ GHN nếu đơn hàng cần đồng bộ
     * Đồng bộ cho đơn hàng có ghn_order_code (kể cả đã Cancelled để đảm bảo đồng bộ với GHN)
     */
    private void syncOrderIfNeeded(Order order) {
        String orderStatus = order.getOrderStatus();
        // Chỉ đồng bộ nếu đơn hàng chưa ở trạng thái cuối cùng (Delivered)
        // Vẫn đồng bộ nếu Cancelled để đảm bảo đồng bộ với GHN khi hủy đơn
        if (orderStatus == null || "Delivered".equals(orderStatus)) {
            return;
        }

        Delivery delivery = deliveryRepository.findByOrderId(order.getId()).orElse(null);
        if (delivery == null || delivery.getGhnOrderCode() == null || delivery.getGhnOrderCode().isEmpty()) {
            return;
        }

        try {
            // Gọi sync nhưng không throw exception nếu lỗi (để không ảnh hưởng đến danh sách)
            syncOrderStatusFromGHNInternal(order.getId(), order, delivery);
        } catch (Exception e) {
            // Log lỗi nhưng không throw - vẫn tiếp tục xử lý các đơn hàng khác
            System.err.println("Auto-sync from GHN failed for order " + order.getId() + " in list: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public OrderDetailResponse getOrderById(Integer orderId, Integer userId, String role) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));

        // Kiểm tra quyền truy cập
        if ("BUYER".equals(role) && !order.getBuyer().getId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if ("SELLER".equals(role) && !order.getSeller().getId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        // Tự động đồng bộ trạng thái từ GHN nếu đơn hàng có ghn_order_code
        // và chưa ở trạng thái cuối cùng (Delivered, Cancelled)
        Delivery delivery = deliveryRepository.findByOrderId(orderId).orElse(null);
        if (delivery != null && delivery.getGhnOrderCode() != null && !delivery.getGhnOrderCode().isEmpty()) {
            String orderStatus = order.getOrderStatus();
            // Chỉ đồng bộ nếu đơn hàng chưa ở trạng thái cuối cùng
            if (orderStatus != null && !"Delivered".equals(orderStatus) && !"Cancelled".equals(orderStatus)) {
                try {
                    // Gọi sync nhưng không throw exception nếu lỗi (để vẫn hiển thị được đơn hàng)
                    // Sử dụng try-catch để không ảnh hưởng đến việc lấy thông tin đơn hàng
                    syncOrderStatusFromGHNInternal(orderId, order, delivery);
                } catch (Exception e) {
                    // Log lỗi nhưng không throw - vẫn trả về thông tin đơn hàng hiện tại
                    System.err.println("Auto-sync from GHN failed for order " + orderId + ": " + e.getMessage());
                    // Không throw exception để vẫn có thể xem đơn hàng
                }
                // Reload order và delivery sau khi sync
                order = orderRepository.findById(orderId).orElse(order);
                delivery = deliveryRepository.findByOrderId(orderId).orElse(delivery);
            }
        }

        // Lấy address từ delivery nếu có
        // Sử dụng getAddressByIdForOrder để lấy địa chỉ kể cả khi đã bị xóa (soft delete)
        // Vì địa chỉ trong đơn hàng cũ vẫn cần hiển thị để xem lịch sử
        BuyerAddressResponse addressResponse = null;
        if (delivery != null && delivery.getAddressId() != null) {
            try {
                addressResponse = shippingService.getAddressByIdForOrder(delivery.getAddressId(), order.getBuyer().getId());
            } catch (Exception e) {
                // Nếu không lấy được address, để null
                System.err.println("Error getting address for order: " + e.getMessage());
            }
        }

        List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        return orderMapper.toOrderDetailResponse(order, addressResponse, delivery, orderDetails);
    }

    /**
     * Internal method để đồng bộ trạng thái từ GHN (không kiểm tra quyền, dùng trong getOrderById)
     * Method này được gọi trong transaction của getOrderById, không cần @Transactional riêng
     */
    private void syncOrderStatusFromGHNInternal(Integer orderId, Order order, Delivery delivery) {
        if (delivery.getGhnOrderCode() == null || delivery.getGhnOrderCode().isEmpty()) {
            return;
        }

        try {
            // Lấy token và shopId từ seller của order
            Integer sellerId = order.getSeller().getId();
            Optional<GHNShopInfo> shopInfoOpt = ghnShopInfoRepository.findBySellerId(sellerId);
            if (!shopInfoOpt.isPresent()) {
                throw new RuntimeException("Seller ID " + sellerId + " chưa cấu hình GHN shop info. Không thể đồng bộ trạng thái từ GHN.");
            }
            GHNShopInfo shopInfo = shopInfoOpt.get();
            String token = shopInfo.getGhnToken();
            Integer shopId = shopInfo.getGhnShopCode();
            
            // Gọi GHN API để lấy trạng thái mới nhất
            GHNOrderDetailData ghnOrderDetail = ghnService.getOrderDetail(delivery.getGhnOrderCode(), token, shopId);

            // Cập nhật trạng thái từ GHN vào Delivery
            if (ghnOrderDetail.getStatus() != null) {
                delivery.setGhnStatus(ghnOrderDetail.getStatus());
            }
            // Map GHN → order_status (dựa trên ghnStatus)
            String oldOrderStatus = order.getOrderStatus();
            String mappedOrderStatus = mapGHNToOrderStatus(order.getOrderStatus(), ghnOrderDetail.getStatus());
            if (mappedOrderStatus != null && !mappedOrderStatus.equals(order.getOrderStatus())) {
                order.setOrderStatus(mappedOrderStatus);
                orderRepository.save(order);
                
                // Nếu GHN hủy đơn (chuyển sang Cancelled), hoàn trả số lượng vào kho
                if ("Cancelled".equals(mappedOrderStatus) && !"Cancelled".equals(oldOrderStatus)) {
                    restoreStockQuantity(orderId);
                }
            }
            if (ghnOrderDetail.getTotalFee() != null) {
                delivery.setGhnTotalFee(BigDecimal.valueOf(ghnOrderDetail.getTotalFee()));
            }

            deliveryRepository.save(delivery);

        } catch (Exception e) {
            System.err.println("Error syncing order status from GHN: " + e.getMessage());
            throw e; // Re-throw để caller có thể handle
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Integer orderId, Integer userId, String role, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));

        // Kiểm tra quyền
        if ("BUYER".equals(role) && !order.getBuyer().getId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if ("SELLER".equals(role) && !order.getSeller().getId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        // Chỉ cho phép hủy nếu đơn ở trạng thái Pending hoặc Paid
        if (!"Pending".equals(order.getOrderStatus()) && !"Paid".equals(order.getOrderStatus())) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }

        // Nếu seller hủy đơn, reason là bắt buộc
        if ("SELLER".equals(role) && (reason == null || reason.trim().isEmpty())) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }

        // Chỉ cho phép hủy khi GHN đang ở trạng thái ready_to_pick (nếu có GHN)
        Delivery delivery = deliveryRepository.findByOrderId(orderId).orElse(null);
        if (delivery != null && delivery.getGhnOrderCode() != null && !delivery.getGhnOrderCode().isEmpty()) {
            try {
                // Lấy token và shopId từ seller của order
                Integer sellerId = order.getSeller().getId();
                Optional<GHNShopInfo> shopInfoOpt = ghnShopInfoRepository.findBySellerId(sellerId);
                if (!shopInfoOpt.isPresent()) {
                    throw new RuntimeException("Seller ID " + sellerId + " chưa cấu hình GHN shop info. Không thể hủy đơn hàng trên GHN.");
                }
                GHNShopInfo shopInfo = shopInfoOpt.get();
                String token = shopInfo.getGhnToken();
                Integer shopId = shopInfo.getGhnShopCode();

                // Kiểm tra trạng thái hiện tại từ GHN: chỉ cho phép khi status = ready_to_pick
                GHNOrderDetailData ghnOrderDetail = ghnService.getOrderDetail(delivery.getGhnOrderCode(), token, shopId);
                String currentStatus = ghnOrderDetail != null ? ghnOrderDetail.getStatus() : null;
                if (currentStatus == null || !currentStatus.toLowerCase().contains("ready_to_pick")) {
                    throw new ApiException(ErrorCode.BAD_REQUEST);
                }
                
                boolean cancelled = ghnService.cancelOrder(delivery.getGhnOrderCode(), token, shopId);
                if (!cancelled) {
                    // GHN không hủy được -> không hủy nội bộ, trả lỗi
                    throw new ApiException(ErrorCode.BAD_REQUEST);
                }

                // Sau khi GHN hủy thành công, set ghn_status ngay và refresh sau
                // GHN có thể chưa cập nhật ngay, nên set trạng thái hủy trước
                delivery.setGhnStatus("cancel");
                deliveryRepository.save(delivery);

//                // Thử refresh lại sau một chút để lấy trạng thái chính xác từ GHN
//                try {
//                    Thread.sleep(500); // Đợi 500ms để GHN cập nhật
//                    GHNOrderDetailData afterCancelDetail = ghnService.getOrderDetail(delivery.getGhnOrderCode(), token, shopId);
//                    if (afterCancelDetail != null && afterCancelDetail.getStatus() != null) {
//                        delivery.setGhnStatus(afterCancelDetail.getStatus());
//                        deliveryRepository.save(delivery);
//                    }
//                } catch (Exception e) {
//                    // Không chặn luồng nếu cập nhật trạng thái thất bại, đã set "cancel" ở trên
//                    System.err.println("Warning: Failed to refresh GHN status after cancel: " + e.getMessage());
//                }
            } catch (Exception e) {
                // Nếu lỗi khi gọi GHN API, vẫn tiếp tục hủy trong DB nhưng log error
                System.err.println("Error calling GHN cancel API: " + e.getMessage());
                e.printStackTrace();
                // Có thể throw exception nếu muốn bắt buộc phải hủy được trên GHN
                // throw new ApiException(ErrorCode.BAD_REQUEST, "Không thể hủy đơn hàng trên GHN");
            }
        }

        // Hoàn trả số lượng vào kho
        restoreStockQuantity(orderId);

        // Cập nhật trạng thái đơn hàng
        order.setOrderStatus("Cancelled");
        orderRepository.save(order);

        // Cập nhật delivery nếu cần lưu các trường khác (không còn delivery_status)
        if (delivery != null) {
            deliveryRepository.save(delivery);
        }

        // Nếu seller hủy đơn, gửi email thông báo đến buyer (bất đồng bộ sau khi transaction commit)
        if ("SELLER".equals(role)) {
            String buyerEmail = order.getBuyer().getUsers().getEmail();
            String sellerName = order.getSeller().getUsers().getFullName();
            if (buyerEmail != null && !buyerEmail.isEmpty()) {
                // Đảm bảo email chỉ được gửi sau khi transaction commit
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        try {
                            mailService.sendOrderCancelledBySellerEmail(buyerEmail, orderId, sellerName, reason);
                        } catch (Exception e) {
                            // Log lỗi nhưng không throw exception để không ảnh hưởng đến việc hủy đơn
                            System.err.println("Error sending cancellation email to buyer: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    /**
     * Đồng bộ trạng thái đơn hàng từ GHN
     * Lấy trạng thái mới nhất từ GHN API và cập nhật vào database
     */
    @Override
    @Transactional
    public OrderDetailResponse syncOrderStatusFromGHN(Integer orderId, Integer userId, String role) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));

        // Kiểm tra quyền truy cập
        if ("BUYER".equals(role) && !order.getBuyer().getId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if ("SELLER".equals(role) && !order.getSeller().getId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        // Lấy delivery để có ghn_order_code
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));

        if (delivery.getGhnOrderCode() == null || delivery.getGhnOrderCode().isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }

        try {
            // Lấy token và shopId từ seller của order
            Integer sellerId = order.getSeller().getId();
            Optional<GHNShopInfo> shopInfoOpt = ghnShopInfoRepository.findBySellerId(sellerId);
            if (!shopInfoOpt.isPresent()) {
                throw new RuntimeException("Seller ID " + sellerId + " chưa cấu hình GHN shop info. Không thể đồng bộ trạng thái từ GHN.");
            }
            GHNShopInfo shopInfo = shopInfoOpt.get();
            String token = shopInfo.getGhnToken();
            Integer shopId = shopInfo.getGhnShopCode();
            
            // Gọi GHN API để lấy trạng thái mới nhất
            GHNOrderDetailData ghnOrderDetail = ghnService.getOrderDetail(delivery.getGhnOrderCode(), token, shopId);

            // Cập nhật trạng thái từ GHN vào Delivery
            if (ghnOrderDetail.getStatus() != null) {
                delivery.setGhnStatus(ghnOrderDetail.getStatus());
            }
            // Map GHN → order_status (không phụ thuộc delivery_status)
            String oldOrderStatus = order.getOrderStatus();
            String mappedOrderStatus = mapGHNToOrderStatus(order.getOrderStatus(), ghnOrderDetail.getStatus());
            if (mappedOrderStatus != null && !mappedOrderStatus.equals(order.getOrderStatus())) {
                order.setOrderStatus(mappedOrderStatus);
                orderRepository.save(order);
                
                // Nếu GHN hủy đơn (chuyển sang Cancelled), hoàn trả số lượng vào kho
                if ("Cancelled".equals(mappedOrderStatus) && !"Cancelled".equals(oldOrderStatus)) {
                    restoreStockQuantity(orderId);
                }
            }

            // Parse expected_delivery_time từ String sang LocalDateTime nếu cần
            // Tạm thời bỏ qua vì GHN trả về String, có thể parse sau nếu cần
            // if (ghnOrderDetail.getExpectedDeliveryTime() != null) {
            //     delivery.setGhnExpectedDeliveryTime(ghnOrderDetail.getExpectedDeliveryTime());
            // }
            if (ghnOrderDetail.getTotalFee() != null) {
                delivery.setGhnTotalFee(BigDecimal.valueOf(ghnOrderDetail.getTotalFee()));
            }

            deliveryRepository.save(delivery);

            // Bỏ block riêng cho cancel vì đã xử lý qua mapGHNToOrderStatus

            // Trả về order detail đã cập nhật
            BuyerAddressResponse addressResponse = null;
            if (delivery.getAddressId() != null) {
                try {
                    addressResponse = shippingService.getAddressByIdForOrder(delivery.getAddressId(), order.getBuyer().getId());
                } catch (Exception e) {
                    System.err.println("Error getting address for order: " + e.getMessage());
                }
            }

            List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
            Delivery deliveryForResponse = deliveryRepository.findByOrderId(orderId).orElse(null);
            return orderMapper.toOrderDetailResponse(order, addressResponse, deliveryForResponse, orderDetails);

        } catch (Exception e) {
            System.err.println("Error syncing order status from GHN: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }
    }

    // Removed delivery_status mapping; order_status is mapped directly from GHN

    /**
     * Map GHN status (ghnStatus) sang order_status của hệ thống
     * Quy tắc (dựa trên chuỗi ghnStatus):
     * - chứa "cancel" -> Cancelled
     * - chứa "delivered" hoặc "success" -> Delivered
     * - chứa "delivering" | "transporting" | "picked" | "storing" -> Shipped
     * - ready_to_pick/picking: KHÔNG tự động nâng lên Paid (vì COD vẫn chưa thanh toán)
     * - mặc định: không thay đổi
     */
    private String mapGHNToOrderStatus(String currentOrderStatus, String ghnStatus) {
        String s = ghnStatus != null ? ghnStatus.toLowerCase() : "";

        if (s.contains("cancel")) {
            return "Cancelled";
        }
        if (s.contains("delivered") || s.contains("success")) {
            return "Delivered";
        }
        if (s.contains("delivering") || s.contains("transporting")
                || s.contains("picked") || s.contains("storing")) {
            return "Shipped";
        }
        // Bỏ logic tự động nâng Pending -> Paid khi ready_to_pick
        // Vì COD vẫn chưa thanh toán, chỉ khi nào payment status = Success mới nên là Paid
        return null; // không đổi
    }

    /**
     * Hoàn trả số lượng sản phẩm vào kho khi hủy đơn hàng
     * Method này được gọi khi đơn hàng bị hủy (bởi buyer hoặc GHN)
     */
    private void restoreStockQuantity(Integer orderId) {
        List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        for (Orderdetail orderDetail : orderDetails) {
            Product product = orderDetail.getProduct();
            Integer currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            Integer returnedQuantity = orderDetail.getQuantity();
            Integer newStock = currentStock + returnedQuantity;
            product.setStockQuantity(newStock);
            productRepository.save(product);
        }
    }
}

