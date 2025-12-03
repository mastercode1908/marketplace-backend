package com.group7.marketplacesystem.commerce.shipping.service.impl;

import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.commerce.cart.entity.Cart;
import com.group7.marketplacesystem.commerce.cart.entity.Cartitem;
import com.group7.marketplacesystem.commerce.cart.repository.CartItemRepository;
import com.group7.marketplacesystem.commerce.cart.repository.CartRepository;
import com.group7.marketplacesystem.commerce.shipping.dto.ghn.*;
import com.group7.marketplacesystem.commerce.shipping.dto.request.BuyerAddressRequest;
import com.group7.marketplacesystem.commerce.shipping.dto.request.GHNShopInfoRequest;
import com.group7.marketplacesystem.commerce.shipping.dto.response.BuyerAddressResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.CartItemResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.CheckoutResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.GHNShopInfoPreviewResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.GHNShopInfoResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.SellerOrderGroup;
import com.group7.marketplacesystem.commerce.shipping.entity.GHNShopInfo;
import com.group7.marketplacesystem.commerce.shipping.mapper.ShippingMapper;
import com.group7.marketplacesystem.commerce.shipping.repository.GHNShopInfoRepository;
import com.group7.marketplacesystem.commerce.shipping.service.GHNService;
import com.group7.marketplacesystem.commerce.shipping.service.ShippingService;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.BuyerAddress;
import com.group7.marketplacesystem.identity.entity.Seller;
import com.group7.marketplacesystem.identity.repository.BuyerAddressRepository;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import com.group7.marketplacesystem.identity.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {
    private final GHNService ghnService;
    private final BuyerAddressRepository buyerAddressRepository;
    private final BuyerRepository buyerRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final GHNShopInfoRepository ghnShopInfoRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final ShippingMapper shippingMapper;
    
    // Giá trị mặc định cho pickup address (nếu không có trong GHNShopInfo)
    // Tạm thời dùng địa chỉ mặc định - có thể cấu hình sau
    private static final Integer DEFAULT_PICKUP_DISTRICT_ID = 1442; // Quận 1, TP.HCM
    private static final String DEFAULT_PICKUP_WARD_CODE = "1A0401"; // Phường Bến Nghé, Quận 1

    @Override
    public List<GHNProvince> getProvinces() {
        String token = getAnyGhnToken();
        return ghnService.getProvinces(token);
    }

    @Override
    public List<GHNDistrict> getDistricts(Integer provinceId) {
        String token = getAnyGhnToken();
        return ghnService.getDistricts(provinceId, token);
    }

    @Override
    public List<GHNWard> getWards(Integer districtId) {
        String token = getAnyGhnToken();
        return ghnService.getWards(districtId, token);
    }

    /**
     * Lấy token GHN từ database (có thể dùng token của bất kỳ seller nào để lấy master data)
     */
    private String getAnyGhnToken() {
        Optional<GHNShopInfo> shopInfoOpt = ghnShopInfoRepository.findAll().stream().findFirst();
        if (shopInfoOpt.isPresent()) {
            return shopInfoOpt.get().getGhnToken();
        }
        throw new RuntimeException("Không tìm thấy GHN token trong database. Vui lòng cấu hình GHN shop info cho ít nhất một seller.");
    }

    @Override
    @Transactional
    public BuyerAddressResponse createAddress(Integer buyerId, BuyerAddressRequest request) {
        Buyer buyer = buyerRepository.getBuyerById(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        // Nếu set làm mặc định, bỏ mặc định của các địa chỉ khác
        if (request.getIsDefault() != null && request.getIsDefault()) {
            buyerAddressRepository.findByBuyerIdAndIsDefaultTrueAndDeletedAtIsNull(buyerId)
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        buyerAddressRepository.save(addr);
                    });
        }

        BuyerAddress address = new BuyerAddress();
        address.setBuyer(buyer);
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setAddressDetail(request.getAddressDetail());
        address.setWardCode(request.getWardCode());
        address.setDistrictId(request.getDistrictId());
        address.setProvinceName(request.getProvinceName());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        address.setCreatedAt(Instant.now());
        address.setUpdatedAt(Instant.now());

        address = buyerAddressRepository.save(address);
        return shippingMapper.toResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuyerAddressResponse> getAddressesByBuyerId(Integer buyerId) {
        // Chỉ lấy địa chỉ chưa bị xóa (deleted_at IS NULL)
        return buyerAddressRepository.findByBuyerIdAndDeletedAtIsNull(buyerId)
                .stream()
                .map(shippingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BuyerAddressResponse getAddressById(Integer addressId, Integer buyerId) {
        BuyerAddress address = buyerAddressRepository.findById(addressId)
                .orElseThrow(() -> new ApiException(ErrorCode.ADDRESS_NOT_FOUND));
        
        // Kiểm tra địa chỉ thuộc về buyer này
        if (!address.getBuyer().getId().equals(buyerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        
        // Kiểm tra địa chỉ chưa bị xóa
        if (address.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        
        return shippingMapper.toResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public BuyerAddressResponse getAddressByIdForOrder(Integer addressId, Integer buyerId) {
        BuyerAddress address = buyerAddressRepository.findById(addressId)
                .orElseThrow(() -> new ApiException(ErrorCode.ADDRESS_NOT_FOUND));
        
        // Kiểm tra địa chỉ thuộc về buyer này
        if (!address.getBuyer().getId().equals(buyerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        
        // KHÔNG kiểm tra deleted_at - cho phép lấy địa chỉ đã bị xóa để hiển thị trong đơn hàng
        return shippingMapper.toResponse(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Integer buyerId, Integer addressId) {
        BuyerAddress address = buyerAddressRepository.findById(addressId)
                .orElseThrow(() -> new ApiException(ErrorCode.ADDRESS_NOT_FOUND));
        
        // Kiểm tra địa chỉ thuộc về buyer này
        if (!address.getBuyer().getId().equals(buyerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        
        // Soft delete - chỉ cập nhật deleted_at
        address.setDeletedAt(Instant.now());
        address.setUpdatedAt(Instant.now());
        buyerAddressRepository.save(address);
    }


    //Xu li logic thanh toan ma giam gia truoc khi checkout
    @Override
    @Transactional(readOnly = true)
    public CheckoutResponse prepareCheckout(Integer buyerId, Integer addressId) {
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_NOT_FOUND));

        BuyerAddress address = buyerAddressRepository.findById(addressId)
                .orElseThrow(() -> new ApiException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getBuyer().getId().equals(buyerId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        List<Cartitem> cartItems = cartItemRepository.findByCartId(cart.getId());
        
        // Kiểm tra số lượng tồn kho trước khi prepare checkout
        for (Cartitem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            Integer requestedQuantity = cartItem.getQuantity();
            Integer availableStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;

            if (availableStock < requestedQuantity) {
                throw new ApiException(ErrorCode.QUANTITY_EXCEEDED_STOCK);
            }
        }
        
        // Nhóm sản phẩm theo seller
        Map<Integer, List<Cartitem>> itemsBySeller = cartItems.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getSeller().getId()));

        List<SellerOrderGroup> sellerOrders = new ArrayList<>();

        //khoi tao tong tien don hang
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalShippingFee = BigDecimal.ZERO;

        for (Map.Entry<Integer, List<Cartitem>> entry : itemsBySeller.entrySet()) {
            Integer sellerId = entry.getKey();
            List<Cartitem> items = entry.getValue();

            List<CartItemResponse> itemResponses = items.stream()
                    .map(shippingMapper::toCartItemResponse)
                    .collect(Collectors.toList());

            BigDecimal subtotal = itemResponses.stream()
                    .map(CartItemResponse::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tính phí vận chuyển từ GHN API
            BigDecimal shippingFee = calculateShippingFeeFromGHN(sellerId, address, items);

            SellerOrderGroup group = new SellerOrderGroup();
            group.setSellerId(sellerId);
            group.setSellerName(items.get(0).getProduct().getSeller().getShopName());
            group.setItems(itemResponses);
            group.setSubtotal(subtotal);
            group.setShippingFee(shippingFee);
            group.setTotal(subtotal.add(shippingFee));

            sellerOrders.add(group);
            totalAmount = totalAmount.add(subtotal);
            totalShippingFee = totalShippingFee.add(shippingFee);
        }

        CheckoutResponse response = new CheckoutResponse();
        response.setSellerOrders(sellerOrders);
        response.setTotalAmount(totalAmount);
        response.setTotalShippingFee(totalShippingFee);
        response.setFinalAmount(totalAmount.add(totalShippingFee));

        return response;
    }

    /**
     * Tính phí vận chuyển từ GHN API dựa trên địa chỉ gửi/nhận và trọng lượng
     * 
     * @param sellerId ID của seller
     * @param buyerAddress Địa chỉ người nhận
     * @param items Danh sách sản phẩm trong giỏ hàng
     * @return Phí vận chuyển (VND)
     */
    private BigDecimal calculateShippingFeeFromGHN(
            Integer sellerId, 
            BuyerAddress buyerAddress, 
            List<Cartitem> items) {
        try {
            // Lấy pickup address từ GHNShopInfo hoặc dùng giá trị mặc định
            Integer fromDistrictId = DEFAULT_PICKUP_DISTRICT_ID;
            String fromWardCode = DEFAULT_PICKUP_WARD_CODE;
            
            Optional<GHNShopInfo> shopInfoOpt = ghnShopInfoRepository.findBySellerId(sellerId);
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

    /**
     * Seller nhập/cập nhật thông tin GHN shop
     * Tự động lấy thông tin shop từ GHN API và lưu vào database
     * Nếu GHN API không có endpoint lấy shop info, chỉ lưu token và shopCode
     * Seller sẽ cần nhập thủ công pickup_address, pickup_district_id, pickup_ward_code sau
     */
    @Override
    @Transactional
    public GHNShopInfoResponse saveOrUpdateGHNShopInfo(Integer sellerId, GHNShopInfoRequest request) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.SELLER_NOT_FOUND));

        // Tìm hoặc tạo mới GHNShopInfo
        Optional<GHNShopInfo> existingOpt = ghnShopInfoRepository.findBySellerId(sellerId);
        GHNShopInfo shopInfo;
        
        if (existingOpt.isPresent()) {
            shopInfo = existingOpt.get();
        } else {
            shopInfo = new GHNShopInfo();
            shopInfo.setSeller(seller);
            shopInfo.setCreatedAt(Instant.now());
        }

        // Verify shopId với GHN API trước khi lưu
        // Bắt buộc phải tìm thấy shop trong GHN mới cho phép lưu
        if (request.getGhnToken() == null || request.getGhnToken().trim().isEmpty()) {
            throw new ApiException(ErrorCode.GHN_TOKEN_INVALID);
        }
        if (request.getGhnShopCode() == null) {
            throw new ApiException(ErrorCode.GHN_SHOP_NOT_FOUND);
        }
        
        try {
            GHNShopInfoData shopData = ghnService.getShopInfo(request.getGhnToken(), request.getGhnShopCode());
            if (shopData == null || shopData.getShopName() == null || shopData.getShopName().trim().isEmpty()) {
                throw new ApiException(ErrorCode.GHN_SHOP_NOT_FOUND);
            }
            // Nếu tìm thấy shop, cập nhật thông tin
            shopInfo.setGhnToken(request.getGhnToken());
            shopInfo.setGhnShopCode(request.getGhnShopCode());
            shopInfo.setGhnShopName(shopData.getShopName());
        } catch (ApiException e) {
            throw e; // Re-throw ApiException với message đã có
        } catch (RuntimeException e) {
            // Kiểm tra message để phân biệt lỗi token hay shop không tìm thấy
            String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (errorMsg.contains("token") || errorMsg.contains("unauthorized") || errorMsg.contains("401")) {
                throw new ApiException(ErrorCode.GHN_TOKEN_INVALID);
            } else {
                throw new ApiException(ErrorCode.GHN_SHOP_NOT_FOUND);
            }
        } catch (Exception e) {
            // Nếu không tìm thấy shop trong GHN, không cho lưu
            throw new ApiException(ErrorCode.GHN_SHOP_NOT_FOUND);
        }

        // Cập nhật pickup address nếu có
        if (request.getPickupAddress() != null && !request.getPickupAddress().trim().isEmpty()) {
            shopInfo.setPickupAddress(request.getPickupAddress().trim());
        }
        if (request.getPickupDistrictId() != null) {
            shopInfo.setPickupDistrictId(request.getPickupDistrictId());
        }
        if (request.getPickupWardCode() != null && !request.getPickupWardCode().trim().isEmpty()) {
            shopInfo.setPickupWardCode(request.getPickupWardCode().trim());
        }
        
        shopInfo.setUpdatedAt(Instant.now());
        shopInfo = ghnShopInfoRepository.save(shopInfo);

        // Map sang response
        GHNShopInfoResponse response = shippingMapper.toGHNShopInfoResponse(shopInfo);
        enrichShopInfoResponse(response, shopInfo.getGhnToken());
        return response;
    }

    /**
     * Lấy thông tin GHN shop của seller
     */
    @Override
    @Transactional(readOnly = true)
    public GHNShopInfoResponse getGHNShopInfo(Integer sellerId) {
        GHNShopInfo shopInfo = ghnShopInfoRepository.findBySellerId(sellerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST));
        GHNShopInfoResponse response = shippingMapper.toGHNShopInfoResponse(shopInfo);
        enrichShopInfoResponse(response, shopInfo.getGhnToken());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateFee(com.group7.marketplacesystem.commerce.shipping.dto.request.CalculateFeeRequest request) {
        try {
            // 1. Lấy thông tin sản phẩm để có cân nặng
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));
            
            Integer weight = product.getWeight() != null ? product.getWeight() : 1000; // Mặc định 1kg
            int totalWeight = weight * request.getQuantity();

            // 2. Lấy thông tin shop của seller
            Optional<GHNShopInfo> shopInfoOpt = ghnShopInfoRepository.findBySellerId(request.getSellerId());
            String ghnToken;
            Integer ghnShopCode;
            Integer fromDistrictId = DEFAULT_PICKUP_DISTRICT_ID;
            String fromWardCode = DEFAULT_PICKUP_WARD_CODE;

            if (shopInfoOpt.isPresent()) {
                GHNShopInfo shopInfo = shopInfoOpt.get();
                ghnToken = shopInfo.getGhnToken();
                ghnShopCode = shopInfo.getGhnShopCode();
                if (shopInfo.getPickupDistrictId() != null) {
                    fromDistrictId = shopInfo.getPickupDistrictId();
                }
                if (shopInfo.getPickupWardCode() != null && !shopInfo.getPickupWardCode().isEmpty()) {
                    fromWardCode = shopInfo.getPickupWardCode();
                }
            } else {
                // Nếu seller chưa cấu hình, thử dùng token mặc định (nếu có) hoặc throw error
                // Ở đây ta throw error để seller biết phải cấu hình
                throw new ApiException(ErrorCode.GHN_SHOP_NOT_FOUND);
            }

            // 3. Tạo request tính phí
            GHNCalculateFeeRequest feeRequest = new GHNCalculateFeeRequest();
            feeRequest.setFromDistrictId(fromDistrictId);
            feeRequest.setFromWardCode(fromWardCode);
            feeRequest.setToDistrictId(request.getToDistrictId());
            feeRequest.setToWardCode(request.getToWardCode());
            feeRequest.setWeight(totalWeight);
            feeRequest.setServiceTypeId(2); // Standard service
            feeRequest.setServiceId(null);

            // 4. Gọi GHN API
            Integer fee = ghnService.calculateShippingFee(feeRequest, ghnToken, ghnShopCode);
            
            if (fee != null && fee > 0) {
                return BigDecimal.valueOf(fee);
            }
            
            return BigDecimal.valueOf(30000); // Fallback

        } catch (Exception e) {
            System.err.println("Error calculating fee: " + e.getMessage());
            return BigDecimal.valueOf(30000); // Fallback
        }
    }

    @Override
    public GHNShopInfoPreviewResponse previewGHNShopInfo(String token, Integer shopCode) {
        // Verify token và shopCode
        if (token == null || token.trim().isEmpty()) {
            throw new ApiException(ErrorCode.GHN_TOKEN_INVALID);
        }
        if (shopCode == null) {
            throw new ApiException(ErrorCode.GHN_SHOP_NOT_FOUND);
        }

        try {
            // Gọi GHN API để lấy shop info
            GHNShopInfoData shopData = ghnService.getShopInfo(token, shopCode);
            if (shopData == null || shopData.getShopName() == null || shopData.getShopName().trim().isEmpty()) {
                throw new ApiException(ErrorCode.GHN_SHOP_NOT_FOUND);
            }

            GHNShopInfoPreviewResponse response = GHNShopInfoPreviewResponse.builder()
                    .shopName(shopData.getShopName())
                    .address(shopData.getAddress())
                    .districtId(shopData.getDistrictId())
                    .wardCode(shopData.getWardCode())
                    .provinceId(shopData.getProvinceId())
                    .build();
            enrichPreviewResponse(response, token);
            return response;
        } catch (ApiException e) {
            throw e; // Re-throw ApiException
        } catch (RuntimeException e) {
            // Kiểm tra message để phân biệt lỗi token hay shop không tìm thấy
            String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (errorMsg.contains("token") || errorMsg.contains("unauthorized") || errorMsg.contains("401")) {
                throw new ApiException(ErrorCode.GHN_TOKEN_INVALID);
            } else {
                throw new ApiException(ErrorCode.GHN_SHOP_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ApiException(ErrorCode.GHN_SHOP_NOT_FOUND);
        }
    }

    /**
    * Bổ sung (enrich) thông tin địa lý (tỉnh, huyện, xã) cho response lấy thông tin shop từ GHN.

    * Vì API của GHN thường trả về ID, nên hàm này dùng token để gọi các API khác và gắn thêm tên tương ứng.
     */
    private void enrichShopInfoResponse(GHNShopInfoResponse response, String token) {
        if (response == null || isBlank(token)) {
            return;
        }

        if (response.getPickupProvinceId() != null) {
            String provinceName = resolveProvinceName(response.getPickupProvinceId(), token);
            response.setPickupProvinceName(provinceName);
        }

        if (response.getPickupDistrictId() != null) {
            GHNDistrict district = resolveDistrict(response.getPickupDistrictId(), response.getPickupProvinceId(), token);
            if (district != null) {
                response.setPickupDistrictName(district.getDistrictName());
                if (response.getPickupProvinceId() == null && district.getProvinceId() != null) {
                    response.setPickupProvinceId(district.getProvinceId());
                    response.setPickupProvinceName(resolveProvinceName(district.getProvinceId(), token));
                }
            }
        }

        if (response.getPickupWardCode() != null && response.getPickupDistrictId() != null) {
            GHNWard ward = resolveWard(response.getPickupDistrictId(), response.getPickupWardCode(), token);
            if (ward != null) {
                response.setPickupWardName(ward.getWardName());
            }
        }
    }

    /**
     * Tương tự hàm trên, nhưng dành cho model preview (GHNShopInfoPreviewResponse).

     * Logics y chang:

     * Điền provinceName

     * Điền districtName

     * Điền wardName

     * Bổ sung province từ district nếu thiếu
     */

    private void enrichPreviewResponse(GHNShopInfoPreviewResponse response, String token) {
        if (response == null || isBlank(token)) {
            return;
        }

        if (response.getProvinceId() != null) {
            String provinceName = resolveProvinceName(response.getProvinceId(), token);
            response.setProvinceName(provinceName);
        }

        if (response.getDistrictId() != null) {
            GHNDistrict district = resolveDistrict(response.getDistrictId(), response.getProvinceId(), token);
            if (district != null) {
                response.setDistrictName(district.getDistrictName());
                if (response.getProvinceId() == null && district.getProvinceId() != null) {
                    response.setProvinceId(district.getProvinceId());
                    response.setProvinceName(resolveProvinceName(district.getProvinceId(), token));
                }
            }
        }

        if (response.getWardCode() != null && response.getDistrictId() != null) {
            GHNWard ward = resolveWard(response.getDistrictId(), response.getWardCode(), token);
            if (ward != null) {
                response.setWardName(ward.getWardName());
            }
        }
    }

    /**
     * Lấy danh sách tỉnh từ GHN (ghnService.getProvinces(token))

     * Tìm province khớp provinceId

     * Trả về provinceName

     * Nếu thất bại → return null.
     */
    private String resolveProvinceName(Integer provinceId, String token) {
        if (provinceId == null || isBlank(token)) {
            return null;
        }
        try {
            return ghnService.getProvinces(token).stream()
                    .filter(province -> provinceId.equals(province.getProvinceId()))
                    .map(GHNProvince::getProvinceName)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Failed to resolve province name: " + e.getMessage());
            return null;
        }
    }

    /**
     * Tìm thông tin huyện theo districtId.

     * Ưu tiên tra theo provinceIdHint trước (nếu biết tỉnh → tìm nhanh hơn).

     * Nếu không có hoặc không tìm thấy:
     * → duyệt tất cả các tỉnh để tìm huyện.
     */
    private GHNDistrict resolveDistrict(Integer districtId, Integer provinceIdHint, String token) {
        if (districtId == null || isBlank(token)) {
            return null;
        }

        if (provinceIdHint != null) {
            GHNDistrict district = fetchDistrictInProvince(provinceIdHint, districtId, token);
            if (district != null) {
                return district;
            }
        }

        try {
            List<GHNProvince> provinces = ghnService.getProvinces(token);
            for (GHNProvince province : provinces) {
                GHNDistrict district = fetchDistrictInProvince(province.getProvinceId(), districtId, token);
                if (district != null) {
                    return district;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to resolve district: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy danh sách huyện của một tỉnh cụ thể bằng ghnService.getDistricts(provinceId, token)

     * Lọc theo districtId

     * Trả về GHNDistrict nếu tìm thấy.
     */
    private GHNDistrict fetchDistrictInProvince(Integer provinceId, Integer districtId, String token) {
        if (provinceId == null || isBlank(token)) {
            return null;
        }
        try {
            return ghnService.getDistricts(provinceId, token).stream()
                    .filter(district -> districtId.equals(district.getDistrictId()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Failed to fetch districts for province " + provinceId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy danh sách phường/xã theo districtId

     * Tìm ward theo wardCode

     * Trả về GHNWard.
     */
    private GHNWard resolveWard(Integer districtId, String wardCode, String token) {
        if (districtId == null || wardCode == null || isBlank(token)) {
            return null;
        }
        try {
            return ghnService.getWards(districtId, token).stream()
                    .filter(ward -> wardCode.equalsIgnoreCase(ward.getWardCode()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Failed to resolve ward: " + e.getMessage());
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
