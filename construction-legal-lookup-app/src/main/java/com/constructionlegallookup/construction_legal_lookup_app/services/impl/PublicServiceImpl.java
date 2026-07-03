package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.CategoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.PopularDocumentResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.RecentDocumentResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.StatsResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Document;
import com.constructionlegallookup.construction_legal_lookup_app.mappers.DocumentMapper;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.CategoryRepository;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.DocumentRepository;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.specs.DocumentSpecifications;
import com.constructionlegallookup.construction_legal_lookup_app.services.PublicService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class PublicServiceImpl implements PublicService {

    DocumentRepository documentRepository;
    CategoryRepository categoryRepository;
    DocumentMapper documentMapper;

    @Override
    public StatsResponse getStats() {
        long totalDocs = documentRepository.count(DocumentSpecifications.isNotDeleted());
        long uniqueTypes = documentRepository.countUniqueDocumentTypes();
        long uniqueBodies = documentRepository.countUniqueIssuingBodies();

        // Get dataset update date
        LocalDateTime maxUpdated = documentRepository.findAll(
                DocumentSpecifications.isNotDeleted(),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "updatedAt"))
        ).stream().findFirst().map(Document::getUpdatedAt).orElse(LocalDateTime.now());

        String label = "Dữ liệu cập nhật đến " + maxUpdated.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        return StatsResponse.builder()
                .totalDocuments(totalDocs)
                .totalTypes(uniqueTypes)
                .totalIssuingBodies(uniqueBodies)
                .datasetUpdatedAt(maxUpdated)
                .datasetLabel(label)
                .build();
    }

    @Override
    public List<RecentDocumentResponse> getRecentDocuments(int limit) {
        int finalLimit = Math.min(limit, 20);
        return documentRepository.findAll(
                DocumentSpecifications.isNotDeleted(),
                PageRequest.of(0, finalLimit, Sort.by(Sort.Direction.DESC, "issuedDate"))
        ).stream()
                .map(documentMapper::toRecentDocumentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PopularDocumentResponse> getPopularDocuments(int limit) {
        int finalLimit = Math.min(limit, 20);
        return documentRepository.findAll(
                DocumentSpecifications.isNotDeleted(),
                PageRequest.of(0, finalLimit, Sort.by(Sort.Direction.DESC, "viewCount"))
        ).stream()
                .map(documentMapper::toPopularDocumentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecentDocumentResponse> getUpdatedDocuments(int limit) {
        int finalLimit = Math.min(limit, 20);
        return documentRepository.findAll(
                DocumentSpecifications.isNotDeleted(),
                PageRequest.of(0, finalLimit, Sort.by(Sort.Direction.DESC, "updatedAt"))
        ).stream()
                .map(documentMapper::toRecentDocumentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getCategories() {
        List<Object[]> results = categoryRepository.findAllCategoriesWithCount();
        return results.stream().map(row -> CategoryResponse.builder()
                .id((Long) row[0])
                .name((String) row[1])
                .slug((String) row[2])
                .documentCount((Long) row[3])
                .build()
        ).collect(Collectors.toList());
    }
}
