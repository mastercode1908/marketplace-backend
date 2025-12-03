package com.group7.marketplacesystem.commerce.cart.service;

import com.group7.marketplacesystem.commerce.cart.dto.request.AddCartItemRequest;
import com.group7.marketplacesystem.commerce.cart.dto.request.UpdateCartItemQuantityRequest;
import com.group7.marketplacesystem.commerce.cart.entity.Cart;
import com.group7.marketplacesystem.commerce.shipping.dto.response.CartResponse;

public interface CartService {
    CartResponse getCartByBuyerId(Integer buyerId);

    void addCartItem(Integer buyerId, AddCartItemRequest request);
    
    CartResponse updateCartItemQuantity(Integer buyerId, Integer itemId, UpdateCartItemQuantityRequest request);
    
    CartResponse deleteCartItem(Integer buyerId, Integer itemId);
    
    void clearCart(Integer buyerId);

    Cart createCart(Integer buyerId);

}


