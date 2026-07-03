package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.RelationAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.RelationAdminDto;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Document;
import com.constructionlegallookup.construction_legal_lookup_app.entities.DocumentRelation;
import com.constructionlegallookup.construction_legal_lookup_app.enums.RelationType;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.AppException;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.ErrorCode;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.DocumentRelationRepository;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.DocumentRepository;
import com.constructionlegallookup.construction_legal_lookup_app.services.AdminRelationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class AdminRelationServiceImpl implements AdminRelationService {

    DocumentRelationRepository documentRelationRepository;
    DocumentRepository documentRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<RelationAdminDto> getRelationsByDocumentId(Long documentId) {
        List<DocumentRelation> sourceRelations = documentRelationRepository.findBySourceDocumentId(documentId);
        List<DocumentRelation> targetRelations = documentRelationRepository.findByTargetDocumentId(documentId);
        
        List<RelationAdminDto> result = new ArrayList<>();
        result.addAll(mapToDtoList(sourceRelations));
        result.addAll(mapToDtoList(targetRelations));
        return result;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RelationAdminDto createRelation(RelationAdminRequest request) {
        Document sourceDoc = documentRepository.findById(request.getSourceDocumentId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        Document targetDoc = documentRepository.findById(request.getTargetDocumentId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        
        DocumentRelation relation = DocumentRelation.builder()
                .sourceDocument(sourceDoc)
                .targetDocument(targetDoc)
                .relationType(RelationType.valueOf(request.getRelationType()))
                .note(request.getNote())
                .build();
        
        DocumentRelation saved = documentRelationRepository.save(relation);
        return mapToDto(saved);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteRelation(Long relationId) {
        documentRelationRepository.deleteById(relationId);
    }
    
    private List<RelationAdminDto> mapToDtoList(List<DocumentRelation> relations) {
        return relations.stream().map(this::mapToDto).toList();
    }
    
    private RelationAdminDto mapToDto(DocumentRelation relation) {
        return RelationAdminDto.builder()
                .id(relation.getId())
                .sourceDocumentId(relation.getSourceDocument().getId())
                .targetDocumentId(relation.getTargetDocument().getId())
                .relationType(relation.getRelationType().name())
                .note(relation.getNote())
                .targetDocument(RelationAdminDto.TargetDocumentBrief.builder()
                        .id(relation.getTargetDocument().getId())
                        .documentNumber(relation.getTargetDocument().getDocumentNumber())
                        .title(relation.getTargetDocument().getTitle())
                        .build())
                .build();
    }
}
