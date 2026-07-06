package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.CategoryAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.CategoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Category;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.AppException;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.ErrorCode;
import com.constructionlegallookup.construction_legal_lookup_app.mappers.CategoryMapper;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.CategoryRepository;
import com.constructionlegallookup.construction_legal_lookup_app.services.AdminCategoryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class AdminCategoryServiceImpl implements AdminCategoryService {

    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<CategoryResponse> getAllCategories() {
        List<Object[]> results = categoryRepository.findAllCategoriesWithCount();
        return results.stream().map(row -> {
            Long id = (Long) row[0];
            String name = (String) row[1];
            String slug = (String) row[2];
            Long documentCount = (Long) row[3];
            return CategoryResponse.builder()
                    .id(id)
                    .name(name)
                    .slug(slug)
                    .documentCount(documentCount != null ? documentCount : 0L)
                    .build();
        }).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryResponse createCategory(CategoryAdminRequest request) {
        Category category = categoryMapper.toCategory(request);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(saved);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryAdminRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryMapper.updateCategory(category, request);
        Category updated = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updated);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
