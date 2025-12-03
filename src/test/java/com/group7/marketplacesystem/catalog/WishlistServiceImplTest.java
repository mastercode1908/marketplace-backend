package com.group7.marketplacesystem.catalog;

import com.group7.marketplacesystem.catalog.dto.request.WishlistRequest;
import com.group7.marketplacesystem.catalog.dto.response.WishlistResponse;
import com.group7.marketplacesystem.catalog.entity.Product;
import com.group7.marketplacesystem.catalog.entity.Wishlist;
import com.group7.marketplacesystem.catalog.entity.WishlistId;
import com.group7.marketplacesystem.catalog.mapper.WishlistMapper;
import com.group7.marketplacesystem.catalog.repository.ProductRepository;
import com.group7.marketplacesystem.catalog.repository.WishlistRepository;
import com.group7.marketplacesystem.catalog.service.impl.WishlistServiceImpl;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.identity.entity.Buyer;
import com.group7.marketplacesystem.identity.entity.User;
import com.group7.marketplacesystem.identity.repository.BuyerRepository;
import com.group7.marketplacesystem.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Wishlist Service Tests")
class WishlistServiceImplTest {

    @Mock private WishlistRepository wishlistRepository;
    @Mock private ProductRepository productRepository;
    @Mock private BuyerRepository buyerRepository;
    @Mock private UserRepository userRepository;
    @Mock private WishlistMapper wishlistMapper;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks private WishlistServiceImpl wishlistService;

    private Wishlist wishlist;
    private Product product;
    private Buyer buyer;
    private User user;
    private WishlistId wishlistId;
    private WishlistRequest wishlistRequest;
    private WishlistResponse wishlistResponse;

    @BeforeEach
    void setUp() {
        // User
        user = new User();
        user.setId(1);
        user.setEmail("buyer@example.com");

        // Buyer
        buyer = new Buyer();
        buyer.setId(1);
        buyer.setUsers(user);

        // Product
        product = new Product();
        product.setId(1);
        product.setName("iPhone 15 Pro");
        product.setPrice(new BigDecimal("29999000"));

        // WishlistId
        wishlistId = new WishlistId();
        wishlistId.setBuyerId(1);
        wishlistId.setProductId(1);

        // Wishlist
        wishlist = new Wishlist();
        wishlist.setId(wishlistId);
        wishlist.setBuyer(buyer);
        wishlist.setProduct(product);
        wishlist.setAddedAt(Instant.now());

        // Request
        wishlistRequest = new WishlistRequest();
        wishlistRequest.setProductId(1);

        // Response
        wishlistResponse = new WishlistResponse();
        wishlistResponse.setProductId(1);
        wishlistResponse.setProductName("iPhone 15 Pro");
        wishlistResponse.setPrice(new BigDecimal("29999000"));
    }

    // -------------------- CREATE --------------------
    @Test
    @DisplayName("Should add product to wishlist successfully")
    void testCreateWishlist_Success() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");

        when(productRepository.findById(anyInt())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.of(buyer));
        when(wishlistRepository.existsById(any(WishlistId.class))).thenReturn(false);
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);

        String result = wishlistService.creatWishlist(wishlistRequest);

        assertThat(result).isEqualTo("Bạn đã thêm sản phẩm này vào wishlist");
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate product to wishlist")
    void testCreateWishlist_DuplicateProduct() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");

        when(productRepository.findById(anyInt())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.of(buyer));
        when(wishlistRepository.existsById(any(WishlistId.class))).thenReturn(true);

        assertThatThrownBy(() -> wishlistService.creatWishlist(wishlistRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Sản phẩm đã có trong wishlist!");
        verify(wishlistRepository, never()).save(any(Wishlist.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testCreateWishlist_ProductNotFound() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");

        when(productRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.creatWishlist(wishlistRequest))
                .isInstanceOf(ApiException.class);
        verify(wishlistRepository, never()).save(any(Wishlist.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw exception when buyer not found")
    void testCreateWishlist_BuyerNotFound() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");

        when(productRepository.findById(anyInt())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.creatWishlist(wishlistRequest))
                .isInstanceOf(ApiException.class);
        verify(wishlistRepository, never()).save(any(Wishlist.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should handle authentication failure when creating wishlist")
    void testCreateWishlist_AuthenticationFailure() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        when(productRepository.findById(anyInt())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.creatWishlist(wishlistRequest))
                .isInstanceOf(ApiException.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should verify wishlist added time is set")
    void testCreateWishlist_AddedTimeSet() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");

        when(productRepository.findById(anyInt())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.of(buyer));
        when(wishlistRepository.existsById(any(WishlistId.class))).thenReturn(false);

        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(invocation -> {
            Wishlist savedWishlist = invocation.getArgument(0);
            assertThat(savedWishlist.getAddedAt()).isNotNull();
            return savedWishlist;
        });

        wishlistService.creatWishlist(wishlistRequest);
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
        SecurityContextHolder.clearContext();
    }

    // -------------------- GET ALL --------------------
    @Test
    @DisplayName("Should get all wishlists for current user")
    void testGetAllWishlists_Success() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.of(buyer));

        Wishlist wishlist2 = new Wishlist();
        wishlist2.setProduct(product);
        List<Wishlist> wishlists = Arrays.asList(wishlist, wishlist2);
        Page<Wishlist> wishlistPage = new PageImpl<>(wishlists);

        when(wishlistRepository.findAllByBuyer(any(Buyer.class), any(Pageable.class)))
                .thenReturn(wishlistPage);
        when(wishlistMapper.toResponse(any(Wishlist.class))).thenReturn(wishlistResponse);

        // sửa ở đây: nhận Page, lấy content để so sánh
        Page<WishlistResponse> resultPage = wishlistService.getAllWishlists(Pageable.unpaged());
        List<WishlistResponse> result = resultPage.getContent();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(wishlistRepository, times(1)).findAllByBuyer(buyer, Pageable.unpaged());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return empty list when no wishlists found")
    void testGetAllWishlists_EmptyList() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.of(buyer));

        Page<Wishlist> emptyPage = new PageImpl<>(List.of());
        when(wishlistRepository.findAllByBuyer(any(Buyer.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<WishlistResponse> resultPage = wishlistService.getAllWishlists(Pageable.unpaged());
        List<WishlistResponse> result = resultPage.getContent();
        assertThat(result).isEmpty();
        SecurityContextHolder.clearContext();
    }

    // -------------------- DELETE --------------------
    @Test
    @DisplayName("Should delete wishlist item successfully")
    void testDeleteWishlist_Success() {
        Long productId = 1L;

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.of(buyer));
        when(wishlistRepository.existsById(any(WishlistId.class))).thenReturn(true);
        doNothing().when(wishlistRepository).deleteById(any(WishlistId.class));

        String result = wishlistService.deleteWishlist(productId);

        assertThat(result).isEqualTo("Bạn đã xóa sản phẩm này ra khỏi wishlist");
        verify(wishlistRepository, times(1)).deleteById(any(WishlistId.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent wishlist item")
    void testDeleteWishlist_NotFound() {
        Long productId = 999L;

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("buyer@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(buyerRepository.findById(anyInt())).thenReturn(Optional.of(buyer));
        when(wishlistRepository.existsById(any(WishlistId.class))).thenReturn(false);

        assertThatThrownBy(() -> wishlistService.deleteWishlist(productId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Sản phẩm này không có trong wishlist!");
        verify(wishlistRepository, never()).deleteById(any(WishlistId.class));
        SecurityContextHolder.clearContext();
    }

    // -------------------- SEARCH --------------------
    @Test
    @DisplayName("Should search wishlist by product name")
    void testSearchWishlist_Success() {
        String searchText = "iPhone";
        List<Wishlist> wishlists = Arrays.asList(wishlist);

        when(wishlistRepository.searchByProductNameContaining(anyString())).thenReturn(wishlists);
        when(wishlistMapper.toResponse(any(Wishlist.class))).thenReturn(wishlistResponse);

        List<WishlistResponse> result = wishlistService.searchWishlist(searchText);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(wishlistRepository, times(1)).searchByProductNameContaining(searchText);
    }

    @Test
    @DisplayName("Should return empty list when search finds no results")
    void testSearchWishlist_NoResults() {
        String searchText = "NonExistentProduct";

        when(wishlistRepository.searchByProductNameContaining(anyString())).thenReturn(List.of());

        List<WishlistResponse> result = wishlistService.searchWishlist(searchText);

        assertThat(result).isEmpty();
        verify(wishlistRepository, times(1)).searchByProductNameContaining(searchText);
    }
}
