package com.group7.marketplacesystem.commerce.cart;

import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Productmedia;
import com.group7.marketplacesystem.commerce.cart.dto.request.UpdateCartItemQuantityRequest;
import com.group7.marketplacesystem.commerce.cart.entity.Cart;
import com.group7.marketplacesystem.commerce.cart.entity.Cartitem;
import com.group7.marketplacesystem.commerce.cart.repository.CartItemRepository;
import com.group7.marketplacesystem.commerce.cart.repository.CartRepository;
import com.group7.marketplacesystem.commerce.cart.service.impl.CartServiceImpl;
import com.group7.marketplacesystem.commerce.shipping.dto.response.CartItemResponse;
import com.group7.marketplacesystem.commerce.shipping.dto.response.CartResponse;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import com.group7.marketplacesystem.identity.entity.Seller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl Full Test Coverage")
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private com.group7.marketplacesystem.catalog.repository.ProductmediaRepository productMediaRepository;

    @InjectMocks private CartServiceImpl cartService;

    private Cart cart;
    private Cartitem cartItem;
    private Product product;
    private Productmedia media;

    @BeforeEach
    void setUp() {
        // Product
        product = new Product();
        product.setId(1);
        product.setName("iPhone 15 Pro");
        product.setStockQuantity(10);
        product.setPrice(new BigDecimal("29999000"));
        product.setSeller(new com.group7.marketplacesystem.identity.entity.Seller());
        product.getSeller().setId(1);
        product.getSeller().setShopName("AppleStore");

        // Product media
        media = new Productmedia();
        media.setId(1);
        media.setUrl("http://image.url");
        media.setPosition(1);

        // Cart
        cart = new Cart();
        cart.setId(1);

        // CartItem
        cartItem = new Cartitem();
        cartItem.setId(1);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(product.getPrice());
    }

    //================ getCartByBuyerId =================
    @Test
    void testGetCartByBuyerId_Success() {
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(productMediaRepository.findByProductId(product.getId())).thenReturn(List.of(media));

        CartResponse response = cartService.getCartByBuyerId(1);

        assertThat(response.getCartId()).isEqualTo(cart.getId());
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductImage()).isEqualTo("http://image.url");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
    }

    @Test
    void testGetCartByBuyerId_CartNotFound() {
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cartService.getCartByBuyerId(1))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Cart not found");
    }

    @Test
    void testGetCartByBuyerId_ProductNoMedia() {
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(productMediaRepository.findByProductId(product.getId())).thenReturn(Collections.emptyList());

        CartResponse response = cartService.getCartByBuyerId(1);
        assertThat(response.getItems().get(0).getProductImage()).isEmpty();
    }

    //================ updateCartItemQuantity =================
    @Test
    void testUpdateCartItemQuantity_Success() {
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(productMediaRepository.findByProductId(product.getId())).thenReturn(List.of(media));

        UpdateCartItemQuantityRequest req = new UpdateCartItemQuantityRequest();
        req.setQuantity(5);

        CartResponse response = cartService.updateCartItemQuantity(1, cartItem.getId(), req);

        assertThat(cartItem.getQuantity()).isEqualTo(5);
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(5);
    }


    @Test
    void testUpdateCartItemQuantity_ExceedsStock() {
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(cartItem));
        UpdateCartItemQuantityRequest req = new UpdateCartItemQuantityRequest();
        req.setQuantity(100);

        assertThatThrownBy(() -> cartService.updateCartItemQuantity(1, 1, req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("The quantity of this product in stock is insufficient. Please check your shopping cart again.");
    }

    @Test
    void testUpdateCartItemQuantity_CartNotFound() {
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.empty());
        UpdateCartItemQuantityRequest req = new UpdateCartItemQuantityRequest();
        req.setQuantity(1);
        assertThatThrownBy(() -> cartService.updateCartItemQuantity(1, 1, req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Cart not found");
    }

    @Test
    void testUpdateCartItemQuantity_CartItemNotFound() {
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1)).thenReturn(Optional.empty());
        UpdateCartItemQuantityRequest req = new UpdateCartItemQuantityRequest();
        req.setQuantity(1);
        assertThatThrownBy(() -> cartService.updateCartItemQuantity(1, 1, req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCode.CART_ITEM_NOT_FOUND.getMessage());
    }

    @Test
    void testUpdateCartItemQuantity_Unauthorized() {
        Cart otherCart = new Cart();
        otherCart.setId(99);
        cartItem.setCart(otherCart);
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(cartItem));
        UpdateCartItemQuantityRequest req = new UpdateCartItemQuantityRequest();
        req.setQuantity(1);

        assertThatThrownBy(() -> cartService.updateCartItemQuantity(1, 1, req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCode.UNAUTHORIZED.getMessage());
    }

    //================ deleteCartItem =================
    @Test
    void testDeleteCartItem_Success() {
        List<Cartitem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);

        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.findByCartId(cart.getId())).thenAnswer(invocation -> new ArrayList<>(cartItems));

        doAnswer(invocation -> {
            cartItems.remove(cartItem);
            return null;
        }).when(cartItemRepository).delete(cartItem);

        CartResponse response = cartService.deleteCartItem(1, 1);

        verify(cartItemRepository).delete(cartItem);
        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void testDeleteCartItem_CartNotFound() {
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cartService.deleteCartItem(1, 1))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCode.CART_NOT_FOUND.getMessage());
    }

    @Test
    void testDeleteCartItem_ItemNotFound() {
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cartService.deleteCartItem(1, 1))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCode.CART_ITEM_NOT_FOUND.getMessage());
    }

    @Test
    void testDeleteCartItem_Unauthorized() {
        Cart otherCart = new Cart();
        otherCart.setId(99);
        cartItem.setCart(otherCart);
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1)).thenReturn(Optional.of(cartItem));
        assertThatThrownBy(() -> cartService.deleteCartItem(1, 1))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("You do not have permission");
    }

    //================ clearCart =================
    @Test
    void testClearCart_Success() {
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.of(cart));
        cartService.clearCart(1);
        verify(cartItemRepository).deleteByCartId(cart.getId());
    }

    @Test
    void testClearCart_CartNotFound() {
        when(cartRepository.findByBuyerId(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cartService.clearCart(1))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Cart not found");
    }

    //================ mapToCartItemResponse edge case =================
    @Test
    @DisplayName("Should map cart item to response even if product has no media")
    void testMapToCartItemResponse_ProductNoMedia() {
        Integer buyerId = 1;

        // Setup product with no media
        Product product = new Product();
        product.setId(1);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("10000"));
        product.setStockQuantity(10);
        product.setSeller(new Seller());
        product.getSeller().setId(1);
        product.getSeller().setShopName("Shop A");

        // Setup cart and cart item
        Cart cart = new Cart();
        cart.setId(1);
        Cartitem cartItem = new Cartitem();
        cartItem.setId(1);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(product.getPrice());

        // Mock repository
        when(cartRepository.findByBuyerId(buyerId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(productMediaRepository.findByProductId(product.getId())).thenReturn(List.of()); // no media

        // Call service
        CartResponse response = cartService.getCartByBuyerId(buyerId);

        // Assertions
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductImage()).isEmpty();
    }
}
