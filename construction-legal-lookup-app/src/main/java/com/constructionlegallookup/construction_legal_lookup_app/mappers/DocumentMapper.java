package com.constructionlegallookup.construction_legal_lookup_app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.DocumentAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.DocumentAdminDto;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.DocumentBriefDto;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.DocumentDetailResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.DocumentSearchResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.PopularDocumentResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.RecentDocumentResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Document;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, TagMapper.class})
public interface DocumentMapper {

    RecentDocumentResponse toRecentDocumentResponse(Document document);

    PopularDocumentResponse toPopularDocumentResponse(Document document);

    DocumentSearchResponse toDocumentSearchResponse(Document document);

    DocumentDetailResponse toDocumentDetailResponse(Document document);

    DocumentBriefDto toDocumentBriefDto(Document document);

    DocumentAdminDto toDocumentAdminDto(Document document);

    @Mapping(target = "documentType", expression = "java(request.getDocumentType() != null ? com.constructionlegallookup.construction_legal_lookup_app.enums.DocumentType.valueOf(request.getDocumentType()) : null)")
    @Mapping(target = "status", expression = "java(request.getStatus() != null ? com.constructionlegallookup.construction_legal_lookup_app.enums.DocumentStatus.valueOf(request.getStatus()) : null)")
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Document toDocument(DocumentAdminRequest request);

    @Mapping(target = "documentType", expression = "java(request.getDocumentType() != null ? com.constructionlegallookup.construction_legal_lookup_app.enums.DocumentType.valueOf(request.getDocumentType()) : null)")
    @Mapping(target = "status", expression = "java(request.getStatus() != null ? com.constructionlegallookup.construction_legal_lookup_app.enums.DocumentStatus.valueOf(request.getStatus()) : null)")
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateDocument(@MappingTarget Document document, DocumentAdminRequest request);
}
