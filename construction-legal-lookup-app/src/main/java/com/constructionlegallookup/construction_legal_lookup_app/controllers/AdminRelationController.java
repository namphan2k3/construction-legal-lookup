package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.RelationAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.RelationAdminDto;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.services.AdminRelationService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin - Relations", description = "Admin document relation management")
public class AdminRelationController {

    AdminRelationService adminRelationService;

    @GetMapping("/documents/{id}/relations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RelationAdminDto>>> getDocumentRelations(@PathVariable Long id) {
        List<RelationAdminDto> relations = adminRelationService.getRelationsByDocumentId(id);
        ApiResponse<List<RelationAdminDto>> response = ApiResponse.<List<RelationAdminDto>>builder()
                .success(true)
                .data(relations)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/relations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RelationAdminDto>> createRelation(@Valid @RequestBody RelationAdminRequest request) {
        RelationAdminDto created = adminRelationService.createRelation(request);
        ApiResponse<RelationAdminDto> response = ApiResponse.<RelationAdminDto>builder()
                .success(true)
                .data(created)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/relations/{relationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRelation(@PathVariable Long relationId) {
        adminRelationService.deleteRelation(relationId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }
}
