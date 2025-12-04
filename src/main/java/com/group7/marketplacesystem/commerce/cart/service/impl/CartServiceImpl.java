package com.group7.marketplacesystem.commerce.cart.service.impl;

import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Productmedia;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.catalog.repository.ProductmediaRepository;
import com.group7.marketplacesystem.commerce.cart.dto.request.AddCartItemRequest;
import com.group7.marketplacesystem.commerce.cart.dto.request.UpdateCartItemQuantityRequest;
import com.group7.marketplacesystem.commerce.cart.entity.Cart;
import com.group7.marketplacesystem.commerce.cart.entity.Cartitem;
import com.group7.marketplacesystem.commerce.cart.repository.CartItemRepository;
import com.group7.marketplacesystem.commerce.cart.repository.CartRepository;
import com.group7.marketplacesystem.commerce.cart.service.CartService;
import com.group7.marketplacesystem.commerce.shipping.dto.response.CartItemResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.CartResponse;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductmediaRepository productMediaRepository;
    private final ProductRepository productRepository;
    private final BuyerRepository buyerRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartByBuyerId(Integer buyerId) {
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_NOT_FOUND));

        List<Cartitem> cartItems = cartItemRepository.findByCartId(cart.getId());
        
        List<CartItemResponse> items = cartItems.stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        response.setBuyerId(buyerId);
        response.setItems(items);
        response.setTotalAmount(totalAmount);

        return response;
    }

    @Override
    @Transactional
    public void addCartItem(Integer buyerId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(buyerId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

        int requestedQuantity = request.getQuantity() == null ? 1 : request.getQuantity();
        int stockQuantity = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        
        if (stockQuantity <= 0) {
            throw new ApiException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
        Optional<Cartitem> existingCartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());
        
        if (existingCartItem.isPresent()) {
            // Nếu đã có trong giỏ hàng, tăng số lượng
            Cartitem cartItem = existingCartItem.get();
            int currentQuantity = cartItem.getQuantity();
            int newTotalQuantity = currentQuantity + requestedQuantity;
            
            // Validate tổng số lượng không vượt quá stock
            if (newTotalQuantity > stockQuantity) {
                throw new ApiException(ErrorCode.TOTAL_QUANTITY_EXCEEDED_STOCK);
            }
            
            cartItem.setQuantity(newTotalQuantity);
            cartItemRepository.save(cartItem);
        } else {
            // Nếu chưa có trong giỏ hàng, tạo mới
            // Validate số lượng đang thêm không vượt quá stock
            if (requestedQuantity > stockQuantity) {
                throw new ApiException(ErrorCode.QUANTITY_EXCEEDED_STOCK);
            }

            Cartitem cartItem = new Cartitem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(requestedQuantity);
            cartItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(cartItem);
        }

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItemQuantity(Integer buyerId, Integer itemId, UpdateCartItemQuantityRequest request) {
        // Kiểm tra cart tồn tại
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_NOT_FOUND));

        // Tìm cart item
        Cartitem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_ITEM_NOT_FOUND));

        // Kiểm tra cart item thuộc về buyer này
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        // Kiểm tra số lượng mới không vượt quá stock quantity
        Product product = cartItem.getProduct();
        Integer stockQuantity = product.getStockQuantity();
        if (stockQuantity == null) {
            stockQuantity = 0;
        }

        if (product.getDeletedAt() != null) {
            throw new ApiException(ErrorCode.PRODUCT_IS_DELETE);
        }

        if (request.getQuantity() > stockQuantity) {
            throw new ApiException(ErrorCode.QUANTITY_EXCEEDED_STOCK);
        }

        // Cập nhật số lượng
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        // Trả về cart đã cập nhật
        return getCartByBuyerId(buyerId);
    }

    @Override
    @Transactional
    public CartResponse deleteCartItem(Integer buyerId, Integer itemId) {
        // Kiểm tra cart tồn tại
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_NOT_FOUND));

        // Tìm cart item
        Cartitem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_ITEM_NOT_FOUND));

        // Kiểm tra cart item thuộc về buyer này
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        // Xóa cart item
        cartItemRepository.delete(cartItem);

        // Trả về cart đã cập nhật
        return getCartByBuyerId(buyerId);
    }

    @Override
    @Transactional
    public void clearCart(Integer buyerId) {
        // Kiểm tra cart tồn tại
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.CART_NOT_FOUND));

        // Xóa tất cả cart items
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private Cart getOrCreateCart(Integer buyerId) {
        return cartRepository.findByBuyerId(buyerId)
                .orElseGet(() -> {
                    Buyer buyer = buyerRepository.getBuyerById(buyerId)
                            .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));
                    Cart cart = new Cart();
                    cart.setBuyer(buyer);
                    Instant now = Instant.now();
                    cart.setCreatedAt(now);
                    cart.setUpdatedAt(now);
                    return cartRepository.save(cart);
                });
    }

    @Override
    public Cart createCart(Integer buyerId) {

        // Check Buyer tồn tại
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new ApiException(ErrorCode.BUYER_NOT_FOUND));

        // Nếu buyer đã có cart → trả về luôn
        Optional<Cart> existingCart = cartRepository.findByBuyerId(buyerId);
        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Tạo cart mới
        Cart cart = new Cart();
        cart.setBuyer(buyer);
        cart.setCreatedAt(Instant.now());
        cart.setUpdatedAt(Instant.now());

        return cartRepository.save(cart);
    }

    private CartItemResponse mapToCartItemResponse(Cartitem cartItem) {
        Product product = cartItem.getProduct();
        
        // Lấy ảnh sản phẩm từ ProductMedia (ưu tiên position nhỏ nhất)
        String productImage = productMediaRepository.findByProductId(product.getId())
                .stream()
                .sorted(Comparator.comparing((Productmedia m) -> m.getPosition() == null ? Integer.MAX_VALUE : m.getPosition()))
                .findFirst()
                .map(Productmedia::getUrl)
                .orElse("");

        CartItemResponse response = new CartItemResponse();
        response.setItemId(cartItem.getId());
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setProductImage(productImage);
        response.setQuantity(cartItem.getQuantity());
        response.setUnitPrice(cartItem.getUnitPrice());
        response.setSubtotal(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        response.setSellerId(product.getSeller().getId());
        response.setSellerName(product.getSeller().getShopName());
        response.setStockQuantity(product.getStockQuantity()); // Thêm stock quantity để frontend validate

        return response;
    }
}

