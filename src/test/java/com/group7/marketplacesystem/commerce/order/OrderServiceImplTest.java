package com.group7.marketplacesystem.commerce.order;

import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.commerce.order.entity.Delivery;
import com.group7.marketplacesystem.commerce.order.entity.Order;
import com.group7.marketplacesystem.commerce.order.entity.Orderdetail;
import com.group7.marketplacesystem.commerce.order.mapper.OrderMapper;
import com.group7.marketplacesystem.commerce.order.repository.DeliveryRepository;
import com.group7.marketplacesystem.commerce.order.repository.OrderDetailRepository;
import com.group7.marketplacesystem.commerce.order.repository.OrderRepository;
import com.group7.marketplacesystem.commerce.order.service.impl.OrderServiceImpl;
import com.group7.marketplacesystem.commerce.shipping.dto.ghn.GHNOrderDetailData;
import com.group7.marketplacesystem.commerce.shipping.dto.response.BuyerAddressResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.OrderDetailResponse;
import com.group7.marketplacesystem.commerce.shipping.entity.GHNShopInfo;
import com.group7.marketplacesystem.commerce.shipping.repository.GHNShopInfoRepository;
import com.group7.marketplacesystem.commerce.shipping.service.GHNService;
import com.group7.marketplacesystem.commerce.shipping.service.ShippingService;
import com.group7.marketplacesystem.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderDetailRepository orderDetailRepository;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private ShippingService shippingService;
    @Mock private GHNService ghnService;
    @Mock private GHNShopInfoRepository ghnShopInfoRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderMapper orderMapper;

    @InjectMocks private OrderServiceImpl orderService;

    private Order order;
    private Delivery delivery;
    private Orderdetail orderDetail;
    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Product setup
        product = new Product();
        product.setId(1);
        product.setStockQuantity(10);

        // OrderDetail setup
        orderDetail = new Orderdetail();
        orderDetail.setId(1);
        orderDetail.setProduct(product);
        orderDetail.setQuantity(2);
        orderDetail.setUnitPrice(BigDecimal.valueOf(100));

        // Order setup
        order = new Order();
        order.setId(1);
        order.setOrderStatus("Pending");

        // Delivery setup
        delivery = new Delivery();
        delivery.setId(1);
        delivery.setOrder(order);
        delivery.setGhnOrderCode("GHN123");
        delivery.setGhnStatus("ready_to_pick");
    }

    // ==================== getOrdersByBuyerId ====================
    @Test
    void testGetOrdersByBuyerId_basic() {
        // Test case: Lấy danh sách đơn hàng của buyer, sync trạng thái nếu cần
        when(orderRepository.findByBuyerId(1)).thenReturn(List.of(order));
        when(deliveryRepository.findByOrderId(1)).thenReturn(Optional.of(delivery));
        when(orderDetailRepository.findByOrderId(1)).thenReturn(List.of(orderDetail));
        when(orderMapper.toOrderDetailResponse(any(), any(), any(), any()))
                .thenReturn(mock(OrderDetailResponse.class));

        List<OrderDetailResponse> result = orderService.getOrdersByBuyerId(1);

        assertEquals(1, result.size());
        verify(orderRepository, times(2)).findByBuyerId(1); // reload sau khi sync
        verify(orderMapper).toOrderDetailResponse(order, null, delivery, List.of(orderDetail));
    }

    // ==================== getOrdersBySellerId ====================
    @Test
    void testGetOrdersBySellerId_basic() {
        // Test case: Lấy danh sách đơn hàng của seller
        when(orderRepository.findBySellerId(2)).thenReturn(List.of(order));
        when(deliveryRepository.findByOrderId(1)).thenReturn(Optional.of(delivery));
        when(orderDetailRepository.findByOrderId(1)).thenReturn(List.of(orderDetail));
        when(orderMapper.toOrderDetailResponse(any(), any(), any(), any()))
                .thenReturn(mock(OrderDetailResponse.class));

        List<OrderDetailResponse> result = orderService.getOrdersBySellerId(2);

        assertEquals(1, result.size());
        verify(orderRepository, times(2)).findBySellerId(2);
    }

    // ==================== getOrderById ====================
    @Test
    void testGetOrderById_authorizedBuyer() throws Exception {
        // Test case: Buyer truy cập đơn hàng hợp lệ
        order.setBuyer(new com.group7.marketplacesystem.identity.entity.Buyer());
        order.getBuyer().setId(1);
        order.setSeller(new com.group7.marketplacesystem.identity.entity.Seller());
        order.getSeller().setId(2);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(deliveryRepository.findByOrderId(1)).thenReturn(Optional.of(delivery));
        when(shippingService.getAddressByIdForOrder(anyInt(), anyInt()))
                .thenReturn(mock(BuyerAddressResponse.class));
        when(orderDetailRepository.findByOrderId(1)).thenReturn(List.of(orderDetail));
        when(orderMapper.toOrderDetailResponse(any(), any(), any(), any()))
                .thenReturn(mock(OrderDetailResponse.class));

        OrderDetailResponse result = orderService.getOrderById(1, 1, "BUYER");
        assertNotNull(result);
    }

    @Test
    void testGetOrderById_unauthorizedBuyer() {
        // Test case: Buyer không phải chủ order → ném ApiException
        order.setBuyer(new com.group7.marketplacesystem.identity.entity.Buyer());
        order.getBuyer().setId(99);
        order.setSeller(new com.group7.marketplacesystem.identity.entity.Seller());
        order.getSeller().setId(2);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(ApiException.class, () -> orderService.getOrderById(1, 1, "BUYER"));
    }

    // ==================== cancelOrder ====================
    @Test
    void testCancelOrder_success() throws Exception {
        // Test case: Hủy đơn thành công, GHN ready_to_pick
        order.setBuyer(new com.group7.marketplacesystem.identity.entity.Buyer());
        order.getBuyer().setId(1);
        order.setSeller(new com.group7.marketplacesystem.identity.entity.Seller());
        order.getSeller().setId(2);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(deliveryRepository.findByOrderId(1)).thenReturn(Optional.of(delivery));
        when(ghnShopInfoRepository.findBySellerId(2)).thenReturn(Optional.of(new GHNShopInfo()));
        when(ghnService.getOrderDetail(anyString(), anyString(), anyInt())).thenReturn(new GHNOrderDetailData());
        when(ghnService.cancelOrder(anyString(), anyString(), anyInt())).thenReturn(true);
        when(orderDetailRepository.findByOrderId(1)).thenReturn(List.of(orderDetail));

        orderService.cancelOrder(1, 1, "BUYER", null);

        assertEquals("Cancelled", order.getOrderStatus());
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    void testCancelOrder_wrongStatus() {
        // Test case: Không thể hủy khi trạng thái đã Delivered
        order.setOrderStatus("Delivered");
        order.setBuyer(new com.group7.marketplacesystem.identity.entity.Buyer());
        order.getBuyer().setId(1);
        order.setSeller(new com.group7.marketplacesystem.identity.entity.Seller());
        order.getSeller().setId(2);

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(ApiException.class, () -> orderService.cancelOrder(1, 1, "BUYER", null));
    }

    @Test
    void testCancelOrder_GHNNotReadyToPick() throws Exception {
        // Test case: GHN status không phải ready_to_pick → vẫn hủy DB nhưng log warning
        order.setBuyer(new com.group7.marketplacesystem.identity.entity.Buyer());
        order.getBuyer().setId(1);
        order.setSeller(new com.group7.marketplacesystem.identity.entity.Seller());
        order.getSeller().setId(2);
        delivery.setGhnStatus("delivering");

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(deliveryRepository.findByOrderId(1)).thenReturn(Optional.of(delivery));
        when(ghnShopInfoRepository.findBySellerId(2)).thenReturn(Optional.of(new GHNShopInfo()));
        when(ghnService.getOrderDetail(anyString(), anyString(), anyInt())).thenReturn(new GHNOrderDetailData());

        orderService.cancelOrder(1, 1, "BUYER", null);
        assertEquals("Cancelled", order.getOrderStatus());
    }

    // ==================== syncOrderStatusFromGHN ====================
    @Test
    void testSyncOrderStatusFromGHN_delivered() throws Exception {
        // Test case: Đồng bộ trạng thái từ GHN → Delivered
        order.setBuyer(new com.group7.marketplacesystem.identity.entity.Buyer());
        order.getBuyer().setId(1);
        order.setSeller(new com.group7.marketplacesystem.identity.entity.Seller());
        order.getSeller().setId(2);

        GHNOrderDetailData ghnDetail = new GHNOrderDetailData();
        ghnDetail.setStatus("delivered");
        ghnDetail.setTotalFee(Integer.valueOf(100));

        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(deliveryRepository.findByOrderId(1)).thenReturn(Optional.of(delivery));
        when(ghnShopInfoRepository.findBySellerId(2)).thenReturn(Optional.of(new GHNShopInfo()));
        when(ghnService.getOrderDetail(any(), any(), any())).thenReturn(ghnDetail);
        when(orderDetailRepository.findByOrderId(1)).thenReturn(List.of(orderDetail));
        when(orderMapper.toOrderDetailResponse(any(), any(), any(), any()))
                .thenReturn(mock(OrderDetailResponse.class));

        OrderDetailResponse result = orderService.syncOrderStatusFromGHN(1, 1, "BUYER");

        assertNotNull(result);
        assertEquals("Delivered", order.getOrderStatus());
    }

    // ==================== mapGHNToOrderStatus ====================
    @Test
    void testMapGHNToOrderStatus_various() throws Exception {
        // Test case: Mapping GHN status → Order status
        var method = OrderServiceImpl.class.getDeclaredMethod("mapGHNToOrderStatus", String.class, String.class);
        method.setAccessible(true);

        assertEquals("Cancelled", method.invoke(orderService, "Pending", "cancel"));
        assertEquals("Delivered", method.invoke(orderService, "Pending", "delivered"));
        assertEquals("Delivered", method.invoke(orderService, "Pending", "success"));
        assertEquals("Shipped", method.invoke(orderService, "Pending", "delivering"));
        assertEquals("Shipped", method.invoke(orderService, "Pending", "transporting"));
        assertEquals("Shipped", method.invoke(orderService, "Pending", "picked"));
        assertEquals("Shipped", method.invoke(orderService, "Pending", "storing"));
        assertNull(method.invoke(orderService, "Pending", "ready_to_pick"));
    }

    // ==================== restoreStockQuantity ====================
    @Test
    void testRestoreStockQuantity_multiple() throws Exception {
        // Test case: Kiểm tra restore stock khi hủy nhiều OrderDetail
        Orderdetail od2 = new Orderdetail();
        od2.setProduct(product);
        od2.setQuantity(3);
        when(orderDetailRepository.findByOrderId(1)).thenReturn(Arrays.asList(orderDetail, od2));

        var method = OrderServiceImpl.class.getDeclaredMethod("restoreStockQuantity", Integer.class);
        method.setAccessible(true);
        method.invoke(orderService, 1);

        // stockQuantity = 10 + 2 + 3 = 15
        assertEquals(15, product.getStockQuantity());
    }
}

