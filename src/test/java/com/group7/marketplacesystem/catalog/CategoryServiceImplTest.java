package com.group7.marketplacesystem.catalog;

import com.group7.marketplacesystem.catalog.dto.request.CategoryRequest;
import com.group7.marketplacesystem.catalog.dto.response.CategoryResponse;
import com.group7.marketplacesystem.catalog.entity.Category;
import com.group7.marketplacesystem.catalog.mapper.CategoryMapper;
import com.group7.marketplacesystem.catalog.repository.CategoryRepository;
import com.group7.marketplacesystem.catalog.service.impl.CategoryServiceImpl;
import com.group7.marketplacesystem.common.exception.ApiException;
import com.group7.marketplacesystem.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Category Service Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1);
        category.setName("Điện thoại");
        category.setDescription("Các sản phẩm điện thoại di động");

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Điện thoại");
        categoryRequest.setDescription("Các sản phẩm điện thoại di động");

        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1);
        categoryResponse.setName("Điện thoại");
        categoryResponse.setDescription("Các sản phẩm điện thoại di động");
    }

    @Test
    @DisplayName("Should create category successfully")
    void testCreateCategory_Success() {
        // Vì service tạo Category trực tiếp, chỉ cần mock save + toResponse
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.createCategory(categoryRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Điện thoại");
        assertThat(result.getDescription()).isEqualTo("Các sản phẩm điện thoại di động");

        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(categoryMapper, times(1)).toResponse(any(Category.class));

        // Không verify toEntity nữa vì service không gọi
        verify(categoryMapper, never()).toEntity(any(CategoryRequest.class));
    }

    @Test
    @DisplayName("Should get all categories with pagination")
    void testGetAllCategory_Success() {
        List<Category> categories = Arrays.asList(
                category,
                createCategory(2, "Laptop", "Máy tính xách tay"),
                createCategory(3, "Phụ kiện", "Phụ kiện điện tử")
        );

        Page<Category> categoryPage = new PageImpl<>(categories);
        Pageable pageable = PageRequest.of(0, 10);

        when(categoryRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(categoryPage);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(categoryResponse);

        List<CategoryResponse> result = categoryService.getAllCategory("", pageable);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        verify(categoryRepository, times(1))
                .findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should update category successfully")
    void testUpdateCategory_Success() {
        // Given
        Integer categoryId = 1;
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Điện thoại thông minh");
        updateRequest.setDescription("Smartphone cao cấp");

        Category updatedCategory = new Category();
        updatedCategory.setId(categoryId);
        updatedCategory.setName("Điện thoại thông minh");
        updatedCategory.setDescription("Smartphone cao cấp");

        CategoryResponse updatedResponse = new CategoryResponse();
        updatedResponse.setId(categoryId);
        updatedResponse.setName("Điện thoại thông minh");
        updatedResponse.setDescription("Smartphone cao cấp");

        when(categoryRepository.findById(anyInt())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(updatedResponse);

        // When
        CategoryResponse result = categoryService.updateCategory(categoryId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Điện thoại thông minh");
        assertThat(result.getDescription()).isEqualTo("Smartphone cao cấp");

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent category")
    void testUpdateCategory_NotFound() {
        // Given
        Integer categoryId = 999;
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, categoryRequest))
            .isInstanceOf(ApiException.class);

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should delete category successfully (soft delete)")
    void testDeleteCategory_Success() {
        // Given
        Integer categoryId = 1;
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.of(category));

        // When
        categoryService.deleteCategory(categoryId);

        // Then
        verify(categoryRepository, times(1)).findById(categoryId);
        // Kiểm tra save với deletedAt được set
        verify(categoryRepository, times(1)).save(argThat(cat -> cat.getDeletedAt() != null));
    }


    @Test
    @DisplayName("Should throw exception when deleting non-existent category")
    void testDeleteCategory_NotFound() {
        // Given
        Integer categoryId = 999;
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
            .isInstanceOf(ApiException.class);

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void testCreateCategory_InvalidName_ThrowsException() {
        CategoryRequest req = new CategoryRequest();
        req.setName("  invalid");
        req.setDescription("Valid description");

        assertThatThrownBy(() -> categoryService.createCategory(req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_CATEGORY_NAME));

    }


    // Helper method
    private Category createCategory(Integer id, String name, String description) {
        Category cat = new Category();
        cat.setId(id);
        cat.setName(name);
        cat.setDescription(description);
        return cat;
    }
}
