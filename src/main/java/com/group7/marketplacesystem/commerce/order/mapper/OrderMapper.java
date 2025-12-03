package com.group7.marketplacesystem.commerce.order.mapper;

import com.group7.marketplacesystem.catalog.entity.Productmedia;
import com.group7.marketplacesystem.catalog.repository.ProductmediaRepository;
import com.group7.marketplacesystem.commerce.order.dto.response.OrderResponse;
import com.group7.marketplacesystem.commerce.order.entity.Delivery;
import com.group7.marketplacesystem.commerce.order.entity.Order;
import com.group7.marketplacesystem.commerce.order.entity.Orderdetail;
import com.group7.marketplacesystem.commerce.shipping.dto.response.BuyerAddressResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.OrderDetailResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.OrderItemResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@lombok.RequiredArgsConstructor
public class OrderMapper {

    private final ProductmediaRepository productMediaRepository;

    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setBuyerId(order.getBuyer().getId());
        response.setSellerId(order.getSeller().getId());
        response.setCartId(order.getCartId());
        response.setPromotionId(order.getPromotion() != null ? order.getPromotion().getId() : null);
        response.setTotalAmount(order.getTotalAmount());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setFinalAmount(order.getFinalAmount());
        response.setNote(order.getNote());
        response.setOrderDate(order.getOrderDate());
        response.setOrderStatus(order.getOrderStatus());
        return response;
    }

    /**
     * Map từ Order entity → OrderDetailResponse
     * @param order Order entity
     * @param address BuyerAddressResponse (có thể null)
     * @param delivery Delivery entity (có thể null)
     * @param orderDetails List Orderdetail (có thể null)
     * @return OrderDetailResponse
     */
    public OrderDetailResponse toOrderDetailResponse(
            Order order, 
            BuyerAddressResponse address, 
            Delivery delivery,
            List<Orderdetail> orderDetails) {
        if (order == null) {
            return null;
        }
        
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderId(order.getId());
        response.setBuyerId(order.getBuyer().getId());
        response.setBuyerName(order.getBuyer().getUsers().getFullName());
        response.setSellerId(order.getSeller().getId());
        response.setSellerName(order.getSeller().getShopName());
        response.setTotalAmount(order.getTotalAmount());
        response.setDiscountAmount(order.getDiscountAmount());
        // Lấy shipping fee từ delivery nếu có, nếu không thì dùng giá trị mặc định
        if (delivery != null && delivery.getShippingFee() != null) {
            response.setShippingFee(delivery.getShippingFee());
        } else {
            response.setShippingFee(BigDecimal.valueOf(30000)); // Tạm thời
        }
        response.setFinalAmount(order.getFinalAmount());
        response.setOrderStatus(order.getOrderStatus());
        response.setOrderDate(order.getOrderDate());

        // Set shipping status (trạng thái vận chuyển)
        // Nếu orderStatus là Cancelled thì hiển thị "Đơn hàng đã bị hủy"
        if ("Cancelled".equals(order.getOrderStatus())) {
            response.setShippingStatus("Đơn hàng đã bị hủy");
        } else if (delivery != null && delivery.getGhnStatus() != null && !delivery.getGhnStatus().isEmpty()) {
            // Nếu không, map từ ghnStatus sang tiếng Việt
            response.setShippingStatus(mapGhnStatusToVietnamese(delivery.getGhnStatus()));
        } else {
            // Nếu không có ghnStatus, để null hoặc mặc định
            response.setShippingStatus(null);
        }

        // Set delivery info nếu có
        if (delivery != null) {
            response.setTrackingNumber(delivery.getTrackingNumber());
            response.setGhnOrderCode(delivery.getGhnOrderCode());
        }

        // Map order items
        if (orderDetails != null && !orderDetails.isEmpty()) {
            List<OrderItemResponse> items = orderDetails.stream()
                    .map(this::toOrderItemResponse)
                    .collect(Collectors.toList());
            response.setItems(items);
        }

        // Set address nếu có
        response.setShippingAddress(address);

        return response;
    }

    /**
     * Map từ Orderdetail entity → OrderItemResponse
     */
    public OrderItemResponse toOrderItemResponse(Orderdetail orderDetail) {
        if (orderDetail == null) {
            return null;
        }
        OrderItemResponse item = new OrderItemResponse();
        item.setProductId(orderDetail.getProduct().getId());
        item.setProductName(orderDetail.getProduct().getName());
        String productImage = productMediaRepository.findByProductId(orderDetail.getProduct().getId())
                .stream()
                .sorted(Comparator.comparing((Productmedia m) -> m.getPosition() == null ? Integer.MAX_VALUE : m.getPosition()))
                .findFirst()
                .map(Productmedia::getUrl)
                .orElse("");
        item.setProductImage(productImage);
        item.setQuantity(orderDetail.getQuantity());
        item.setUnitPrice(orderDetail.getUnitPrice());
        item.setSubtotal(orderDetail.getSubtotal());
        item.setIsReviewed(orderDetail.getIsReviewed());
        item.setIsReported(orderDetail.getIsReported());
        item.setOrderDetailId(orderDetail.getId());
        return item;
    }

    /**
     * Map GHN status sang tiếng Việt để hiển thị cho người dùng
     * @param ghnStatus Trạng thái từ GHN (ví dụ: ready_to_pick, delivering, delivered, etc.)
     * @return Trạng thái bằng tiếng Việt
     */
    private String mapGhnStatusToVietnamese(String ghnStatus) {
        if (ghnStatus == null || ghnStatus.isEmpty()) {
            return null;
        }
        
        String status = ghnStatus.toLowerCase();
        
        // Map các trạng thái GHN sang tiếng Việt
        // Kiểm tra các trạng thái cụ thể trước (dài hơn)
        if (status.contains("ready_to_pick")) {
            return "Chờ lấy hàng";
        }
        if (status.contains("picked")) {
            return "Đã lấy hàng";
        }
        if (status.contains("picking")) {
            return "Đang lấy hàng";
        }
        if (status.contains("storing")) {
            return "Đã nhập kho";
        }
        if (status.contains("transporting")) {
            return "Đang vận chuyển";
        }
        if (status.contains("delivering")) {
            return "Đang giao hàng";
        }
        if (status.contains("delivered") || status.contains("success")) {
            return "Đã giao hàng";
        }
        if (status.contains("cancel") || status.contains("cancelled")) {
            return "Đã hủy";
        }
        if (status.contains("return")) {
            return "Hoàn trả";
        }
        if (status.contains("lost")) {
            return "Thất lạc";
        }
        
        // Nếu không match, trả về nguyên bản
        return ghnStatus;
    }
}
