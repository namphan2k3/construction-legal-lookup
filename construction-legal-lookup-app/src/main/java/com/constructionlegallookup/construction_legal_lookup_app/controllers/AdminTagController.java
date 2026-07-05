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

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.TagAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.TagResponse;
import com.constructionlegallookup.construction_legal_lookup_app.services.AdminTagService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin - Tags", description = "Admin tag management")
public class AdminTagController {

    AdminTagService adminTagService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags() {
        List<TagResponse> tags = adminTagService.getAllTags();
        ApiResponse<List<TagResponse>> response = ApiResponse.<List<TagResponse>>builder()
                .success(true)
                .data(tags)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(@Valid @RequestBody TagAdminRequest request) {
        TagResponse created = adminTagService.createTag(request);
        ApiResponse<TagResponse> response = ApiResponse.<TagResponse>builder()
                .success(true)
                .data(created)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable Long id, @Valid @RequestBody TagAdminRequest request) {
        TagResponse updated = adminTagService.updateTag(id, request);
        ApiResponse<TagResponse> response = ApiResponse.<TagResponse>builder()
                .success(true)
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        adminTagService.deleteTag(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }
}
