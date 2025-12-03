package com.group7.marketplacesystem.catalog.controller;

import com.group7.marketplacesystem.catalog.dto.request.CategoryRequest;
import com.group7.marketplacesystem.catalog.dto.response.CategoryResponse;
import com.group7.marketplacesystem.catalog.entity.Category;
import com.group7.marketplacesystem.catalog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>>getAllCategory(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10, page = 0) Pageable pageable){
        List<CategoryResponse> responses = categoryService.getAllCategory(keyword, pageable);
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponse>createCategory(@Valid @RequestBody CategoryRequest request){
        CategoryResponse response = categoryService.createCategory(request);
        return  ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'CONTENTADMIN')")
    @PutMapping("/{id}/update")
    public ResponseEntity<CategoryResponse>update(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryRequest request){
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String>deleteCategory(@PathVariable Integer id){
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Đã xóa thành công.");
    }

}
