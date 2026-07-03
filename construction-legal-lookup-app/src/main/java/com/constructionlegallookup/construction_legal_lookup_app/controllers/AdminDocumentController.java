package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.DocumentAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.DocumentAdminDto;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.UploadPdfResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.PaginationInfo;
import com.constructionlegallookup.construction_legal_lookup_app.services.AdminDocumentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin - Documents", description = "Admin document management")
public class AdminDocumentController {

    AdminDocumentService adminDocumentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<DocumentAdminDto>>> getDocuments(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DocumentAdminDto> documentsPage = adminDocumentService.getAllDocuments(q, categorySlug, 
                documentType, status, year, includeDeleted, pageable);
        
        PaginationInfo pagination = PaginationInfo.builder()
                .page(page)
                .size(size)
                .totalElements(documentsPage.getTotalElements())
                .totalPages(documentsPage.getTotalPages())
                .first(documentsPage.isFirst())
                .last(documentsPage.isLast())
                .build();

        ApiResponse<Page<DocumentAdminDto>> response = ApiResponse.<Page<DocumentAdminDto>>builder()
                .success(true)
                .data(documentsPage)
                .paginationInfo(pagination)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DocumentAdminDto>> createDocument(@Valid @RequestBody DocumentAdminRequest request) {
        DocumentAdminDto created = adminDocumentService.createDocument(request);
        ApiResponse<DocumentAdminDto> response = ApiResponse.<DocumentAdminDto>builder()
                .success(true)
                .data(created)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DocumentAdminDto>> updateDocument(
            @PathVariable Long id, @Valid @RequestBody DocumentAdminRequest request) {
        DocumentAdminDto updated = adminDocumentService.updateDocument(id, request);
        ApiResponse<DocumentAdminDto> response = ApiResponse.<DocumentAdminDto>builder()
                .success(true)
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> softDeleteDocument(@PathVariable Long id) {
        adminDocumentService.softDeleteDocument(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> restoreDocument(@PathVariable Long id) {
        adminDocumentService.restoreDocument(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/upload-pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UploadPdfResponse>> uploadPdf(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) throws IOException {
        UploadPdfResponse result = adminDocumentService.uploadPdf(id, file);
        ApiResponse<UploadPdfResponse> response = ApiResponse.<UploadPdfResponse>builder()
                .success(true)
                .data(result)
                .build();
        return ResponseEntity.ok(response);
    }
}
