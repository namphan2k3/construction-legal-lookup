package com.constructionlegallookup.construction_legal_lookup_app.services;

import java.util.List;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.RelationAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.RelationAdminDto;

public interface AdminRelationService {
    List<RelationAdminDto> getRelationsByDocumentId(Long documentId);
    RelationAdminDto createRelation(RelationAdminRequest request);
    void deleteRelation(Long relationId);
}
