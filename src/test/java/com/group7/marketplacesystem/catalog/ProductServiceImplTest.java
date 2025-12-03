    package com.group7.marketplacesystem.catalog;

    import com.group7.marketplacesystem.catalog.dto.request.ProductCreateRequest;
    import com.group7.marketplacesystem.catalog.dto.request.ProductRequest;
    import com.group7.marketplacesystem.catalog.dto.response.ProductDetailResponse;
    import com.group7.marketplacesystem.catalog.dto.response.ProductInfoResponse;
    import com.group7.marketplacesystem.catalog.entity.Category;
    import com.group7.marketplacesystem.catalog.entity.Product;
    import com.group7.marketplacesystem.catalog.entity.Productmedia;
    import com.group7.marketplacesystem.catalog.mapper.ProductMapper;
    import com.group7.marketplacesystem.catalog.repository.CategoryRepository;
    import com.group7.marketplacesystem.catalog.repository.ProductRepository;
    import com.group7.marketplacesystem.catalog.repository.ProductmediaRepository;
    import com.group7.marketplacesystem.catalog.service.impl.ProductServiceImpl;
    import com.group7.marketplacesystem.common.exception.ApiException;
    import com.group7.marketplacesystem.common.security.CurrentUser;
    import com.group7.marketplacesystem.identity.entity.Seller;
    import com.group7.marketplacesystem.identity.repository.SellerRepository;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.extension.ExtendWith;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.MockedStatic;
    import org.mockito.junit.jupiter.MockitoExtension;

    import java.util.List;
    import java.util.Optional;

    import static org.assertj.core.api.Assertions.*;
    import static org.mockito.Mockito.*;
    import static org.mockito.ArgumentMatchers.*;

    @ExtendWith(MockitoExtension.class)
    public class ProductServiceImplTest {

        @Mock
        ProductRepository productRepository;

        @Mock
        ProductmediaRepository productMediaRepository;

        @Mock
        CategoryRepository categoryRepository;

        @Mock
        SellerRepository sellerRepository;

        @Mock
        ProductMapper productMapper;

        @InjectMocks
        ProductServiceImpl productService;

        // =========================================================================
    // 1. CREATE PRODUCT - SUCCESS
    // =========================================================================
        @Test
        void testCreateProduct_Success() {
            try (MockedStatic<CurrentUser> mocked = mockStatic(CurrentUser.class)) {
                mocked.when(CurrentUser::getUserId).thenReturn(10);

                Seller seller = new Seller();
                seller.setId(10);

                Category category = new Category();
                category.setId(50);

                ProductCreateRequest request = new ProductCreateRequest();
                request.setCategoryId(50);

                Product product = new Product();
                product.setId(1);

                when(sellerRepository.findById(10)).thenReturn(Optional.of(seller));
                when(categoryRepository.findById(50)).thenReturn(Optional.of(category));
                when(productRepository.save(any(Product.class))).thenReturn(product);
                when(productMapper.toResponse(eq(product), anyList()))
                        .thenReturn(new ProductInfoResponse());

                ProductInfoResponse result = productService.createProduct(request);

                assertThat(result).isNotNull();
                verify(productRepository).save(any());
            }
        }

        // =========================================================================
    // 2. CREATE PRODUCT - SELLER NOT FOUND
    // =========================================================================
        @Test
        void testCreateProduct_SellerNotFound() {
            try (MockedStatic<CurrentUser> mocked = mockStatic(CurrentUser.class)) {
                mocked.when(CurrentUser::getUserId).thenReturn(100);

                ProductCreateRequest request = new ProductCreateRequest();
                request.setCategoryId(1);

                when(sellerRepository.findById(100)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> productService.createProduct(request))
                        .isInstanceOf(ApiException.class);
            }
        }

        // =========================================================================
    // 3. CREATE PRODUCT - CATEGORY NOT FOUND
    // =========================================================================
        @Test
        void testCreateProduct_CategoryNotFound() {
            try (MockedStatic<CurrentUser> mocked = mockStatic(CurrentUser.class)) {
                mocked.when(CurrentUser::getUserId).thenReturn(10);

                ProductCreateRequest request = new ProductCreateRequest();
                request.setCategoryId(500);

                Seller seller = new Seller();
                seller.setId(10);

                when(sellerRepository.findById(10)).thenReturn(Optional.of(seller));
                when(categoryRepository.findById(500)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> productService.createProduct(request))
                        .isInstanceOf(ApiException.class);
            }
        }

        @Test
        void testUpdateProduct_Success() {
            try (MockedStatic<CurrentUser> mockedCurrentUser = mockStatic(CurrentUser.class);
                 MockedStatic<ProductMapper> mockedMapper = mockStatic(ProductMapper.class)) {

                mockedCurrentUser.when(CurrentUser::getUserId).thenReturn(5);

                Seller seller = new Seller();
                seller.setId(5);

                Product product = new Product();
                product.setSeller(seller);

                Category category = new Category();
                category.setId(77);

                ProductCreateRequest request = new ProductCreateRequest();
                request.setCategoryId(77);

                // Stub repository
                when(productRepository.findById(100)).thenReturn(Optional.of(product));
                when(categoryRepository.findById(77)).thenReturn(Optional.of(category));

                // Stub mapper static
                mockedMapper.when(() -> ProductMapper.toResponse(product, List.of()))
                        .thenReturn(new ProductInfoResponse());

                ProductInfoResponse result = productService.updateProduct(100, request);

                assertThat(result).isNotNull();
                verify(productRepository).save(product);
            }
        }




        // =========================================================================
    // 5. UPDATE PRODUCT - UNAUTHORIZED
    // =========================================================================
        @Test
        void testUpdateProduct_Unauthorized() {
            try (MockedStatic<CurrentUser> mocked = mockStatic(CurrentUser.class)) {
                mocked.when(CurrentUser::getUserId).thenReturn(99);

                Product product = new Product();
                Seller seller = new Seller();
                seller.setId(1);
                product.setSeller(seller);

                when(productRepository.findById(100))
                        .thenReturn(Optional.of(product));

                assertThatThrownBy(() -> productService.updateProduct(100, new ProductCreateRequest()))
                        .isInstanceOf(ApiException.class);
            }
        }

        // =========================================================================
    // 6. GET PRODUCT BY ID - SUCCESS
    // =========================================================================
        @Test
        void testGetProductById_Success() {

            try (MockedStatic<ProductMapper> mockedMapper = mockStatic(ProductMapper.class)){
            Product product = new Product();
            product.setId(10);

            when(productRepository.findById(10)).thenReturn(Optional.of(product));
            when(productMediaRepository.findByProductIdAndDeletedAtIsNull(10))
                    .thenReturn(List.of());
                mockedMapper.when(() -> ProductMapper.toResponse(product, List.of()))
                        .thenReturn(new ProductInfoResponse());
            ProductInfoResponse result = productService.getProductById(10);

            assertThat(result).isNotNull();
            }
        }

        // =========================================================================
    // 7. GET PRODUCT BY ID - NOT FOUND
    // =========================================================================
        @Test
        void testGetProductById_NotFound() {
            when(productRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(999))
                    .isInstanceOf(ApiException.class);
        }

        // =========================================================================
    // 8. DELETE PRODUCT - SUCCESS
    // =========================================================================
        @Test
        void testDeleteProduct_Success() {
            try (MockedStatic<CurrentUser> mocked = mockStatic(CurrentUser.class)) {
                mocked.when(CurrentUser::getUserId).thenReturn(10);

                Product product = new Product();
                Seller seller = new Seller();
                seller.setId(10);
                product.setSeller(seller);

                when(productRepository.findById(10)).thenReturn(Optional.of(product));
                when(productMediaRepository.findByProductIdAndDeletedAtIsNull(10))
                        .thenReturn(List.of());

                productService.deleteProduct(10);

                assertThat(product.getDeletedAt()).isNotNull();
                verify(productRepository).save(product);
            }
        }

        // =========================================================================
    // 9. DELETE PRODUCT - NOT FOUND
    // =========================================================================
        @Test
        void testDeleteProduct_NotFound() {
            when(productRepository.findById(55)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(55))
                    .isInstanceOf(ApiException.class);
        }

        // =========================================================================
    // 10. GET MY PRODUCTS
    // =========================================================================
        @Test
        void testGetMyProducts() {
            try (MockedStatic<CurrentUser> mockedCurrentUser = mockStatic(CurrentUser.class);
                 MockedStatic<ProductMapper> mockedMapper = mockStatic(ProductMapper.class)) {

                mockedCurrentUser.when(CurrentUser::getUserId).thenReturn(7);

                Product p = new Product();
                p.setId(1);

                when(productRepository.findBySellerIdAndDeletedAtIsNullOrderByIdDesc(7))
                        .thenReturn(List.of(p));
                when(productMediaRepository.findByProductIdAndDeletedAtIsNull(1))
                        .thenReturn(List.of());

                // Sửa matcher list thành any()
                mockedMapper.when(() -> ProductMapper.toResponse(eq(p), any()))
                        .thenReturn(new ProductInfoResponse());

                List<ProductInfoResponse> result = productService.getMyProducts();

                assertThat(result).hasSize(1);
            }

        }

        // =========================================================================
    // 11. GET ALL PENDING PRODUCTS
    // =========================================================================
        @Test
        void testGetAllPendingProducts() {
            Product p1 = new Product();
            p1.setProductStatus("Pending");

            Product p2 = new Product();
            p2.setProductStatus("Approved");

            when(productRepository.findAll()).thenReturn(List.of(p1, p2));
            when(productMapper.toProductDetailResponse(any()))
                    .thenReturn(new ProductDetailResponse());

            List<ProductDetailResponse> result = productService.getAllPendingProduct();

            assertThat(result).hasSize(1);
        }

        // =========================================================================
    // 12. UPDATE STATUS PRODUCT
    // =========================================================================
        @Test
        void testUpdateStatusProduct() {
            Product product = new Product();
            product.setId(1);

            ProductRequest request = new ProductRequest();
            request.setProductStatus("Approved");

            when(productRepository.findById(1)).thenReturn(Optional.of(product));
            when(productMapper.toProductDetailResponse(any()))
                    .thenReturn(new ProductDetailResponse());

            ProductDetailResponse result = productService.updateStatusProduct(1L, request);

            assertThat(product.getProductStatus()).isEqualTo("Approved");
            verify(productRepository).save(product);
        }

        // =========================================================================
    // 13. GET ALL PRODUCTS
    // =========================================================================
        @Test
        void testGetAllProducts() {
            try (MockedStatic<ProductMapper> mockedMapper = mockStatic(ProductMapper.class)) {

                Product p = new Product();
                p.setId(1);

                when(productRepository.findAllByDeletedAtIsNullOrderByIdDesc())
                        .thenReturn(List.of(p));
                when(productMediaRepository.findByProductIdAndDeletedAtIsNull(1))
                        .thenReturn(List.of());
                mockedMapper.when(() -> ProductMapper.toResponse(p, List.of()))
                        .thenReturn(new ProductInfoResponse());

                List<ProductInfoResponse> result = productService.getAllProducts();

                assertThat(result).hasSize(1);
            }
        }
    }
