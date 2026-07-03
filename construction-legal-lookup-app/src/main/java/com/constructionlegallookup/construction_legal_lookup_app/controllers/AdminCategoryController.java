package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.CategoryAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.CategoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.services.AdminCategoryService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin - Categories", description = "Admin category management")
public class AdminCategoryController {

    AdminCategoryService adminCategoryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> categories = adminCategoryService.getAllCategories();
        ApiResponse<List<CategoryResponse>> response = ApiResponse.<List<CategoryResponse>>builder()
                .success(true)
                .data(categories)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryAdminRequest request) {
        CategoryResponse created = adminCategoryService.createCategory(request);
        ApiResponse<CategoryResponse> response = ApiResponse.<CategoryResponse>builder()
                .success(true)
                .data(created)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id, @Valid @RequestBody CategoryAdminRequest request) {
        CategoryResponse updated = adminCategoryService.updateCategory(id, request);
        ApiResponse<CategoryResponse> response = ApiResponse.<CategoryResponse>builder()
                .success(true)
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }
}
