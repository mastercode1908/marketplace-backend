package com.group7.marketplacesystem.commerce.shipping.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.commerce.cart.entity.Cart;
import com.group7.marketplacesystem.commerce.cart.entity.Cartitem;
import com.group7.marketplacesystem.commerce.cart.repository.CartItemRepository;
import com.group7.marketplacesystem.commerce.cart.repository.CartRepository;
import com.group7.marketplacesystem.commerce.order.entity.Delivery;
import com.group7.marketplacesystem.commerce.order.entity.Order;
import com.group7.marketplacesystem.commerce.order.entity.Orderdetail;
import com.group7.marketplacesystem.commerce.order.repository.DeliveryRepository;
import com.group7.marketplacesystem.commerce.order.repository.OrderDetailRepository;
import com.group7.marketplacesystem.commerce.order.repository.OrderRepository;
import com.group7.marketplacesystem.commerce.payment.Service.VNPayService;
import com.group7.marketplacesystem.commerce.payment.entity.Payment;
import com.group7.marketplacesystem.commerce.payment.entity.PaymentSession;
import com.group7.marketplacesystem.commerce.payment.repository.PaymentRepository;
import com.group7.marketplacesystem.commerce.payment.repository.PaymentSessionRepository;
import com.group7.marketplacesystem.commerce.payment.response.VNPayUrlResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.ghn.*;
import com.group7.marketplacesystem.commerce.shipping.dto.request.CheckoutRequest;
import com.group7.marketplacesystem.commerce.shipping.dto.request.SellerPromotionRequest;
import com.group7.marketplacesystem.commerce.shipping.dto.response.BuyerAddressResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.OrderDetailResponse;
import com.group7.marketplacesystem.commerce.shipping.entity.GHNShopInfo;
import com.group7.marketplacesystem.commerce.shipping.repository.GHNShopInfoRepository;
import com.group7.marketplacesystem.commerce.order.mapper.OrderMapper;
import com.group7.marketplacesystem.commerce.shipping.mapper.ShippingMapper;
import com.group7.marketplacesystem.commerce.shipping.service.CheckoutService;
import com.group7.marketplacesystem.commerce.shipping.service.GHNService;
import com.group7.marketplacesystem.commerce.shipping.service.ShippingService;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.common.security.CurrentUser;
import com.group7.marketplacesystem.common.utils.JsonUtil;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.BuyerAddress;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.repository.BuyerAddressRepository;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import com.group7.marketplacesystem.promotion.entity.Promotion;
import com.group7.marketplacesystem.promotion.repository.PromotionRepository;
import com.group7.marketplacesystem.promotion.repository.PromotionUsageRepository;
import com.group7.marketplacesystem.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static com.group7.marketplacesystem.common.utils.VNPayUtils.getRandomNumber;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BuyerRepository buyerRepository;
    private final BuyerAddressRepository buyerAddressRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final DeliveryRepository deliveryRepository;
    private final PaymentRepository paymentRepository;
    private final GHNService ghnService;
    private final GHNShopInfoRepository ghnShopInfoRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final ShippingMapper shippingMapper;
    private final VNPayService vnPayService;
    private final PromotionUsageRepository usageRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionService promotionService;
    private final PaymentSessionRepository paymentSessionRepository;
    private final JsonUtil jsonUtil;

    // Giá trị mặc định cho pickup address (nếu không có trong GHNShopInfo)
    private static final Integer DEFAULT_PICKUP_DISTRICT_ID = 1442; // Quận 1, TP.HCM
    private static final String DEFAULT_PICKUP_WARD_CODE = "1A0401"; // Phường Bến Nghé, Quận 1

    @Override
    @Transactional
    public OrderDetailResponse checkoutCOD(CheckoutRequest request) {

        Integer buyerId = CurrentUser.getUserId();

        Buyer buyer = buyerRepository.getBuyerById(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        BuyerAddress address = buyerAddressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ApiException(ErrorCode.ADDRESS_NOT_FOUND));

        // Mapping sellerId -> promoCode
        Map<Integer, String> promoBySeller = request.getPromotions() == null ?
                Collections.emptyMap() :
                request.getPromotions().stream()
                        .collect(Collectors.toMap(SellerPromotionRequest::getSellerId,
                                SellerPromotionRequest::getPromotionCode));

        // Kiểm tra địa chỉ thuộc về buyer này
        if (!address.getBuyer().getId().equals(buyerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        // Kiểm tra địa chỉ chưa bị xóa
        if (address.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_NOT_FOUND));

        List<Cartitem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new ApiException(ErrorCode.CART_EMPTY);
        }

        // Kiểm tra số lượng tồn kho trước khi đặt hàng
        for (Cartitem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product == null) {
                throw new ApiException(ErrorCode.PRODUCT_IS_DELETE);
            }

            if (product.getDeletedAt() != null) {
                throw new ApiException(ErrorCode.PRODUCT_IS_DELETE);
            }

            Integer requestedQuantity = cartItem.getQuantity();
            Integer availableStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;

            if (availableStock < requestedQuantity) {
                throw new ApiException(ErrorCode.QUANTITY_EXCEEDED_STOCK);
            }
        }


        // Nhóm sản phẩm theo seller
        Map<Integer, List<Cartitem>> itemsBySeller = cartItems.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getSeller().getId()));

        List<Order> createdOrders = new ArrayList<>();

        // Tạo đơn hàng cho từng seller
        for (Map.Entry<Integer, List<Cartitem>> entry : itemsBySeller.entrySet()) {

            Integer sellerIdA = entry.getKey();
            List<Cartitem> items = entry.getValue();
            Seller seller = items.get(0).getProduct().getSeller();

            //Xu ly logic ma giam gia o day
            // Tính tổng tiền
            BigDecimal totalAmount = items.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tính phí vận chuyển từ GHN API
            BigDecimal shippingFee = calculateShippingFeeFromGHN(seller.getId(), address, items);
            BigDecimal finalAmount = totalAmount.add(shippingFee);

            // Áp dụng promotion nếu có
            String promoCode = promoBySeller.get(sellerIdA);
            Promotion appliedPromotion = null;
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (promoCode != null && !promoCode.isEmpty()) {
                appliedPromotion = promotionRepository.findByPromotionCode(promoCode)
                        .orElseThrow(() -> new ApiException(ErrorCode.PROMOTION_NOT_FOUND));
                if (appliedPromotion.getDeletedAt() != null) {
                    throw new ApiException(ErrorCode.PROMOTION_IS_DELETE);
                }

                discountAmount = calculatePromotionDiscount(appliedPromotion, totalAmount,sellerIdA);
            }

            // Trừ discount vào finalAmount
            finalAmount = finalAmount.subtract(discountAmount);


            // Validate COD amount nếu thanh toán COD
            if ("COD".equals(request.getPaymentMethod())) {
                BigDecimal maxCodAmount = BigDecimal.valueOf(50000000); // 50 triệu VND
                if (finalAmount.compareTo(maxCodAmount) > 0) {
                    throw new ApiException(ErrorCode.COD_AMOUNT_EXCEEDED);
                }
            }

            // Tạo đơn hàng
            Order order = new Order();
            order.setBuyer(buyer);
            order.setSeller(seller);
            order.setCartId(cart.getId());
            order.setTotalAmount(totalAmount);
            order.setDiscountAmount(discountAmount);
            order.setShippingFee(shippingFee);
            order.setFinalAmount(finalAmount);
            order.setNote(request.getNote());
            order.setOrderDate(Instant.now());
            order.setOrderStatus("Pending");

            // Gán promotion nếu có
            if (appliedPromotion != null) {
                order.setPromotion(appliedPromotion);
            }

            order = orderRepository.save(order);

            if (appliedPromotion != null) {
                promotionService.savePromotionUsage(appliedPromotion.getId(), buyerId, order.getId());
            }

            // Tạo order details và trừ số lượng trong kho
            for (Cartitem cartItem : items) {
                Orderdetail orderDetail = new Orderdetail();
                orderDetail.setOrder(order);
                orderDetail.setProduct(cartItem.getProduct());
                orderDetail.setQuantity(cartItem.getQuantity());
                orderDetail.setUnitPrice(cartItem.getUnitPrice());
                orderDetailRepository.save(orderDetail);

                // Trừ số lượng trong kho
                Product product = cartItem.getProduct();
                Integer currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                Integer newStock = currentStock - cartItem.getQuantity();
                product.setStockQuantity(newStock);
                productRepository.save(product);
            }

            // Tạo đơn hàng GHN
            // Lấy token và shopId từ GHNShopInfo của seller
            Integer sellerId = items.get(0).getProduct().getSeller().getId();
            Optional<GHNShopInfo> shopInfoOpt = ghnShopInfoRepository.findBySellerId(sellerId);

            if (!shopInfoOpt.isPresent()) {
                throw new ApiException(ErrorCode.GHN_SHOP_INFO_NOT_FOUND);
            }

            GHNShopInfo shopInfo = shopInfoOpt.get();
            String ghnToken = shopInfo.getGhnToken();
            Integer ghnShopCode = shopInfo.getGhnShopCode();

            GHNCreateOrderRequest ghnRequest = buildGHNRequest(items, address, request.getPaymentMethod());
            GHNOrderData ghnOrderData = ghnService.createOrder(ghnRequest, ghnToken, ghnShopCode);

            // Tạo delivery
            Delivery delivery = new Delivery();
            delivery.setOrder(order);
            delivery.setServiceName("GHN");
            delivery.setTrackingNumber(ghnOrderData.getOrderCode());
            delivery.setShippingFee(shippingFee);
            delivery.setGhnOrderCode(ghnOrderData.getOrderCode());
            delivery.setGhnServiceId(1223); // Tạm thời
            delivery.setAddressId(address.getId()); // Lưu addressId để lấy địa chỉ sau này
            if (ghnOrderData.getExpectedDeliveryTime() != null && !ghnOrderData.getExpectedDeliveryTime().isEmpty()) {
                try {
                    delivery.setGhnExpectedDeliveryTime(LocalDateTime.parse(ghnOrderData.getExpectedDeliveryTime()));
                } catch (Exception e) {
                    // Bỏ qua nếu parse lỗi
                }
            }
            delivery.setGhnTotalFee(ghnOrderData.getTotalFee() != null ?
                    BigDecimal.valueOf(ghnOrderData.getTotalFee()) : shippingFee);
            delivery.setGhnCreatedAt(LocalDateTime.now());
            // Lấy trạng thái ban đầu từ GHN sau khi tạo đơn hàng
            try {
                GHNOrderDetailData ghnOrderDetail = ghnService.getOrderDetail(ghnOrderData.getOrderCode(), ghnToken, ghnShopCode);
                if (ghnOrderDetail != null && ghnOrderDetail.getStatus() != null) {
                    delivery.setGhnStatus(ghnOrderDetail.getStatus());
                }
            } catch (Exception e) {
                // Log lỗi nhưng không chặn luồng checkout
                System.err.println("Warning: Failed to get initial GHN status for order " + ghnOrderData.getOrderCode() + ": " + e.getMessage());
            }
            deliveryRepository.save(delivery);

            // Tạo payment
            Payment payment = new Payment();
            payment.setTargetId(order.getId());
            payment.setTargetType("Order");
            payment.setMethod(request.getPaymentMethod());
            payment.setAmount(finalAmount);
            payment.setStatus("Pending");
            payment.setTransactionCode(ghnOrderData.getOrderCode());
            payment.setCreatedAt(Instant.now());
            paymentRepository.save(payment);

            // Cập nhật trạng thái đơn hàng
            order.setOrderStatus("Pending");
            orderRepository.save(order);

            createdOrders.add(order);
        }

        // Xóa giỏ hàng sau khi đặt hàng thành công
        cartItemRepository.deleteByCartId(cart.getId());

        // Trả về đơn hàng đầu tiên (hoặc có thể trả về danh sách)
        Order firstOrder = createdOrders.get(0);
        Delivery delivery = deliveryRepository.findByOrderId(firstOrder.getId()).orElse(null);
        List<Orderdetail> orderDetails = orderDetailRepository.findByOrderId(firstOrder.getId());
        BuyerAddressResponse addressResponse = shippingMapper.toResponse(address);
        return orderMapper.toOrderDetailResponse(firstOrder, addressResponse, delivery, orderDetails);
    }

    @Override
    @Transactional
    public VNPayUrlResponse checkoutVnPay(CheckoutRequest request) {

        Integer buyerId = CurrentUser.getUserId();

        // 1. Check buyer và địa chỉ
        Buyer buyer = buyerRepository.getBuyerById(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        BuyerAddress address = buyerAddressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ApiException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getBuyer().getId().equals(buyerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (address.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        // 2. Check cart
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_NOT_FOUND));
        List<Cartitem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new ApiException(ErrorCode.CART_EMPTY);
        }

        // 3. Check tồn kho
        for (Cartitem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (product == null) {
                throw new ApiException(ErrorCode.PRODUCT_IS_DELETE);
            }

            if (product.getDeletedAt() != null) {
                throw new ApiException(ErrorCode.PRODUCT_IS_DELETE);
            }

            Integer requestedQuantity = cartItem.getQuantity();
            Integer availableStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            if (availableStock < requestedQuantity) {
                throw new ApiException(ErrorCode.QUANTITY_EXCEEDED_STOCK);
            }

        }

        // Mapping sellerId -> promoCode
        Map<Integer, String> promoBySeller = request.getPromotions() == null ?
                Collections.emptyMap() :
                request.getPromotions().stream()
                        .collect(Collectors.toMap(
                                SellerPromotionRequest::getSellerId,
                                SellerPromotionRequest::getPromotionCode
                        ));

        // 4. Nhóm theo seller
        Map<Integer, List<Cartitem>> itemsBySeller = cartItems.stream()
                .collect(Collectors.groupingBy(ci -> ci.getProduct().getSeller().getId()));

        // 5. Tính tổng finalAmount sau khi trừ mã giảm giá
        BigDecimal totalFinalAmount = BigDecimal.ZERO;

        for (Map.Entry<Integer, List<Cartitem>> entry : itemsBySeller.entrySet()) {

            Integer sellerId = entry.getKey();
            List<Cartitem> items = entry.getValue();

            // Tổng sản phẩm
            BigDecimal subtotal = items.stream()
                    .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Shipping fee
            BigDecimal shippingFee = calculateShippingFeeFromGHN(
                    items.get(0).getProduct().getSeller().getId(), address, items);

            BigDecimal sellerFinal = subtotal.add(shippingFee);

            // Áp dụng mã giảm giá → giống COD
            String promoCode = promoBySeller.get(sellerId);
            if (promoCode != null && !promoCode.isEmpty()) {
                Promotion promotion = promotionRepository.findByPromotionCode(promoCode)
                        .orElseThrow(() -> new ApiException(ErrorCode.PROMOTION_NOT_FOUND));

                BigDecimal discount = calculatePromotionDiscount(promotion, subtotal, sellerId);

                if (promotion.getDeletedAt() != null) {
                    throw new ApiException(ErrorCode.PROMOTION_IS_DELETE);
                }
                sellerFinal = sellerFinal.subtract(discount);
            }

            totalFinalAmount = totalFinalAmount.add(sellerFinal);
        }

        // 5. Lưu vào payment_session
        PaymentSession session = new PaymentSession();
        session.setBuyerId(buyerId);
        session.setCartId(cart.getId());
        session.setAddressId(address.getId());
        session.setPromotions(jsonUtil.toJson(request.getPromotions()));  // convert List<SellerPromotionRequest> -> JSON
        session.setNote(request.getNote());
        session.setAmount(totalFinalAmount);
        session.setPaymentMethod("VNPAY");
        session.setStatus("PENDING");
        session.setCreatedAt(Instant.now());

        // Tạo txnRef ngẫu nhiên 8 chữ số
        String txnRef = getRandomNumber(8);  // hoặc method generate unique
        session.setTxnRef(txnRef);

        session = paymentSessionRepository.save(session);

        // 6. Tạo URL VNPAY với final amount đã trừ tất cả mã giảm giá
        String paymentUrl;
        try {
            paymentUrl = vnPayService.createPayment(totalFinalAmount, txnRef);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return new VNPayUrlResponse(paymentUrl);
    }


    @Override
    @Transactional
    public void checkoutFromPaymentSession(Long sessionId) {
        PaymentSession session = paymentSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.SESSION_NOT_FOUND));

        Buyer buyer = buyerRepository.findById(session.getBuyerId())
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        BuyerAddress address = buyerAddressRepository.findById(session.getAddressId())
                .orElseThrow(() -> new ApiException(ErrorCode.ADDRESS_NOT_FOUND));

        List<Cartitem> items = cartItemRepository.findByCartId(session.getCartId());

        List<SellerPromotionRequest> promoList = jsonUtil.fromJson(
                session.getPromotions(),
                new TypeReference<List<SellerPromotionRequest>>() {}
        );

        // Nhóm cart item theo seller
        Map<Integer, List<Cartitem>> itemsBySeller = items.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getSeller().getId()));

        for (Map.Entry<Integer, List<Cartitem>> entry : itemsBySeller.entrySet()) {
            Integer sellerId = entry.getKey();
            List<Cartitem> sellerItems = entry.getValue();
            Seller seller = sellerItems.get(0).getProduct().getSeller();

            BigDecimal totalAmount = sellerItems.stream()
                    .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal shippingFee = calculateShippingFeeFromGHN(seller.getId(), address, sellerItems);
            BigDecimal finalAmount = totalAmount.add(shippingFee);

            // Áp dụng promotion
            SellerPromotionRequest promoReq = promoList.stream()
                    .filter(p -> p.getSellerId().equals(sellerId))
                    .findFirst()
                    .orElse(null);

            Promotion appliedPromotion = null;
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (promoReq != null && promoReq.getPromotionCode() != null) {
                // Cố gắng lấy promotion, nếu không tồn tại thì bỏ qua
                appliedPromotion = promotionRepository.findByPromotionCode(promoReq.getPromotionCode())
                        .orElse(null);

                if (appliedPromotion != null) {
                    discountAmount = calculatePromotionDiscount(appliedPromotion, totalAmount, sellerId);
                } else {
                    discountAmount = BigDecimal.ZERO; // Không có promotion -> giảm 0
                }
            }

            finalAmount = finalAmount.subtract(discountAmount);

            // Tạo Order
            Order order = new Order();
            order.setBuyer(buyer);
            order.setSeller(seller);
            order.setCartId(session.getCartId());
            order.setTotalAmount(totalAmount);
            order.setDiscountAmount(discountAmount);
            order.setShippingFee(shippingFee);
            order.setFinalAmount(finalAmount);
            order.setNote(session.getNote());
            order.setOrderDate(Instant.now());
            order.setOrderStatus("Paid");
            if (appliedPromotion != null) order.setPromotion(appliedPromotion);
            order = orderRepository.save(order);

            // PromotionUsage
            if (appliedPromotion != null) {
                promotionService.savePromotionUsage(appliedPromotion.getId(), buyer.getId(), order.getId());
            }

            // OrderDetail & trừ kho
            for (Cartitem cartItem : sellerItems) {
                Orderdetail detail = new Orderdetail();
                detail.setOrder(order);
                detail.setProduct(cartItem.getProduct());
                detail.setQuantity(cartItem.getQuantity());
                detail.setUnitPrice(cartItem.getUnitPrice());
                orderDetailRepository.save(detail);

                Product product = cartItem.getProduct();
                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                productRepository.save(product);
            }
            Order finalOrder = order; // order đã save rồi, biến tạm final
            BigDecimal finalFinalAmount = finalAmount; // final cho finalAmount nếu cần
            // Delivery & Payment nếu có GHN
            ghnShopInfoRepository.findBySellerId(sellerId).ifPresent(shopInfo -> {
                GHNCreateOrderRequest ghnRequest = buildGHNRequest(sellerItems, address, session.getPaymentMethod());
                GHNOrderData ghnOrderData = ghnService.createOrder(ghnRequest, shopInfo.getGhnToken(), shopInfo.getGhnShopCode());

                Delivery delivery = new Delivery();
                delivery.setOrder(finalOrder);
                delivery.setGhnServiceId(1223); // Tạm thời
                delivery.setServiceName("GHN");
                delivery.setTrackingNumber(ghnOrderData.getOrderCode());
                delivery.setShippingFee(shippingFee);
                delivery.setGhnOrderCode(ghnOrderData.getOrderCode());
                delivery.setAddressId(address.getId());
                delivery.setGhnCreatedAt(LocalDateTime.now());
                // Lấy trạng thái ban đầu từ GHN sau khi tạo đơn hàng
                try {
                    GHNOrderDetailData ghnOrderDetail = ghnService.getOrderDetail(ghnOrderData.getOrderCode(), shopInfo.getGhnToken(), shopInfo.getGhnShopCode());
                    if (ghnOrderDetail != null && ghnOrderDetail.getStatus() != null) {
                        delivery.setGhnStatus(ghnOrderDetail.getStatus());
                    }
                } catch (Exception e) {
                    // Log lỗi nhưng không chặn luồng checkout
                    System.err.println("Warning: Failed to get initial GHN status for order " + ghnOrderData.getOrderCode() + ": " + e.getMessage());
                }
                deliveryRepository.save(delivery);

                Payment payment = new Payment();
                payment.setTargetId(finalOrder.getId());
                payment.setTargetType("Order");
                payment.setMethod(session.getPaymentMethod());
                payment.setAmount(finalFinalAmount);
                payment.setStatus("Paid");
                payment.setTransactionCode(ghnOrderData.getOrderCode());
                payment.setCreatedAt(Instant.now());
                paymentRepository.save(payment);
            });
        }
        // Cuối method checkoutFromPaymentSession, trước hoặc sau khi xóa cart items
        session.setStatus("COMPLETED");
        paymentSessionRepository.save(session);

        // Xóa cart items
        cartItemRepository.deleteByCartId(session.getCartId());
    }



    /**
     * Tính phí vận chuyển từ GHN API dựa trên địa chỉ gửi/nhận và trọng lượng
     * (Tương tự method trong ShippingServiceImpl)
     */
    private BigDecimal calculateShippingFeeFromGHN(
            Integer sellerId,
            BuyerAddress buyerAddress,
            List<Cartitem> items) {
        try {
            // Lấy pickup address từ GHNShopInfo hoặc dùng giá trị mặc định
            Integer fromDistrictId = DEFAULT_PICKUP_DISTRICT_ID;
            String fromWardCode = DEFAULT_PICKUP_WARD_CODE;

            java.util.Optional<GHNShopInfo> shopInfoOpt = ghnShopInfoRepository.findBySellerId(sellerId);
            String ghnToken;
            Integer ghnShopCode;

            if (shopInfoOpt.isPresent()) {
                GHNShopInfo shopInfo = shopInfoOpt.get();
                if (shopInfo.getPickupDistrictId() != null) {
                    fromDistrictId = shopInfo.getPickupDistrictId();
                }
                if (shopInfo.getPickupWardCode() != null && !shopInfo.getPickupWardCode().isEmpty()) {
                    fromWardCode = shopInfo.getPickupWardCode();
                }
                ghnToken = shopInfo.getGhnToken();
                ghnShopCode = shopInfo.getGhnShopCode();
            } else {
                throw new RuntimeException("Seller ID " + sellerId + " chưa cấu hình GHN shop info. Vui lòng cấu hình token và shop code trong phần quản lý GHN shop info.");
            }

            // Tính tổng trọng lượng (gram)
            int totalWeight = items.stream()
                    .mapToInt(item -> {
                        Integer weight = item.getProduct().getWeight();
                        return (weight != null ? weight : 1000) * item.getQuantity(); // Mặc định 1kg nếu null
                    })
                    .sum();

            // Tạo request tính phí
            GHNCalculateFeeRequest feeRequest = new GHNCalculateFeeRequest();
            feeRequest.setFromDistrictId(fromDistrictId);
            feeRequest.setFromWardCode(fromWardCode);
            feeRequest.setToDistrictId(buyerAddress.getDistrictId());
            feeRequest.setToWardCode(buyerAddress.getWardCode());
            feeRequest.setWeight(totalWeight);
            feeRequest.setServiceTypeId(2); // Standard service
            feeRequest.setServiceId(null); // Có thể null

            // Gọi GHN API để tính phí với token và shopId từ database
            Integer fee = ghnService.calculateShippingFee(feeRequest, ghnToken, ghnShopCode);

            if (fee != null && fee > 0) {
                return BigDecimal.valueOf(fee);
            }

            // Nếu API trả về null hoặc 0, dùng giá trị mặc định
            System.err.println("Warning: GHN API returned invalid fee, using default 30000");
            return BigDecimal.valueOf(30000);

        } catch (Exception e) {
            // Nếu có lỗi khi gọi GHN API, log và dùng giá trị mặc định
            System.err.println("Error calculating shipping fee from GHN: " + e.getMessage());
            e.printStackTrace();
            return BigDecimal.valueOf(30000); // Fallback về 30,000 VND
        }
    }

    public BigDecimal calculatePromotionDiscount(Promotion promotion, BigDecimal orderTotal, Integer sellerId) {
        if (promotion == null || orderTotal == null) {
            return BigDecimal.ZERO;
        }

        if (promotion.getDeletedAt() != null) {
            return BigDecimal.ZERO; // coi như đã xóa
        }

        // Check status
        if (!"Active".equalsIgnoreCase(promotion.getPromotionStatus())) {
            return BigDecimal.ZERO;
        }

        // -------------------
        // Check owner type
        // -------------------
        String ownerType = promotion.getOwnerType();
        Integer ownerId = promotion.getOwnerId();

        if ("SELLER".equalsIgnoreCase(ownerType)) {
            if (!ownerId.equals(sellerId)) {
                throw new ApiException(ErrorCode.PROMOTION_WRONG_SELLER);
            }
        }

        // Check ngày bắt đầu và kết thúc
        LocalDate today = LocalDate.now();
        if ((promotion.getStartDate() != null && today.isBefore(promotion.getStartDate()))
                || (promotion.getEndDate() != null && today.isAfter(promotion.getEndDate()))) {
            return BigDecimal.ZERO;
        }

        // Check usage limit
        if (promotion.getUsageLimit() != null && promotion.getUsedCount() >= promotion.getUsageLimit()) {
            return BigDecimal.ZERO;
        }

        // Tính discount
        BigDecimal discount = BigDecimal.ZERO;
        if ("PERCENT".equalsIgnoreCase(promotion.getDiscountType())) {
            discount = orderTotal.multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if ("AMOUNT".equalsIgnoreCase(promotion.getDiscountType())) {
            discount = promotion.getDiscountValue();
        }

        // Giới hạn max discount
        if (promotion.getMaxDiscountAmount() != null) {
            discount = discount.min(promotion.getMaxDiscountAmount());
        }

        // NEW: Không cho discount vượt quá orderTotal
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }

        return discount;
    }



    private GHNCreateOrderRequest buildGHNRequest(List<Cartitem> items, BuyerAddress address, String paymentMethod) {
        GHNCreateOrderRequest request = new GHNCreateOrderRequest();

        // payment_type_id: 1 = Người bán trả, 2 = Người mua trả
        // COD: payment_type_id = 2, cod_amount = finalAmount
        // VNPAY: payment_type_id = 1, cod_amount = 0
        if ("COD".equals(paymentMethod)) {
            request.setPaymentTypeId(2);
            // cod_amount sẽ được tính sau
        } else {
            request.setPaymentTypeId(1);
            request.setCodAmount(0);
        }

        request.setRequiredNote("KHONGCHOXEMHANG");
        request.setToName(address.getReceiverName());
        request.setToPhone(address.getReceiverPhone());

        // Format địa chỉ đầy đủ để GHN có thể convert sang Google Maps
        // Format: "addressDetail, Ward, District, Province"
        // Lưu ý: GHN cần địa chỉ đầy đủ để convert sang Google Maps API
        // Nếu chỉ có addressDetail ngắn, GHN có thể không convert được
        String fullAddress = address.getAddressDetail();
        if (address.getProvinceName() != null && !address.getProvinceName().trim().isEmpty()) {
            // Thêm province name vào cuối địa chỉ để GHN dễ convert hơn
            fullAddress = fullAddress + ", " + address.getProvinceName();
        }
        request.setToAddress(fullAddress);
        request.setToWardCode(address.getWardCode());
        request.setToDistrictId(address.getDistrictId());
        request.setServiceTypeId(2); // Standard

        // Tính tổng trọng lượng và tạo items
        int totalWeight = 0;
        List<GHNItem> ghnItems = new ArrayList<>();

        for (Cartitem cartItem : items) {
            Product product = cartItem.getProduct();
            int itemWeight = product.getWeight() != null ? product.getWeight() : 1000; // Mặc định 1kg nếu null
            totalWeight += itemWeight * cartItem.getQuantity();

            GHNItem ghnItem = new GHNItem();
            ghnItem.setName(product.getName());
            ghnItem.setCode(String.valueOf(product.getId()));
            ghnItem.setQuantity(cartItem.getQuantity());
            ghnItem.setPrice(cartItem.getUnitPrice().intValue());
            ghnItem.setWeight(itemWeight);

            GHNCategory category = new GHNCategory();
            category.setLevel1(product.getCategory().getName());
            ghnItem.setCategory(category);

            ghnItems.add(ghnItem);
        }

        request.setWeight(totalWeight);
        request.setItems(ghnItems);

        // Tính cod_amount cho COD
        if ("COD".equals(paymentMethod)) {
            BigDecimal totalAmount = items.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            // Tính phí vận chuyển từ GHN API (sử dụng cùng logic như khi tạo order)
            BigDecimal shippingFee = calculateShippingFeeFromGHN(
                    items.get(0).getProduct().getSeller().getId(),
                    address,
                    items
            );
            request.setCodAmount(totalAmount.add(shippingFee).intValue());
        }

        return request;
    }

}

