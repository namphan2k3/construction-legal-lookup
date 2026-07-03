package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.DocumentAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.DocumentAdminDto;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.UploadPdfResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Category;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Document;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Tag;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.AppException;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.ErrorCode;
import com.constructionlegallookup.construction_legal_lookup_app.mappers.DocumentMapper;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.CategoryRepository;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.DocumentRepository;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.TagRepository;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.specs.DocumentSpecifications;
import com.constructionlegallookup.construction_legal_lookup_app.services.AdminDocumentService;
import com.constructionlegallookup.construction_legal_lookup_app.services.PdfExtractionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class AdminDocumentServiceImpl implements AdminDocumentService {

    DocumentRepository documentRepository;
    CategoryRepository categoryRepository;
    TagRepository tagRepository;
    DocumentMapper documentMapper;
    Cloudinary cloudinary;
    PdfExtractionService pdfExtractionService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<DocumentAdminDto> getAllDocuments(String q, String categorySlug, String documentType, 
                                                    String status, Integer year, Boolean includeDeleted, Pageable pageable) {
        Specification<Document> spec = Specification.where(null);
        
        if (!Boolean.TRUE.equals(includeDeleted)) {
            spec = spec.and((root, query, cb) -> cb.isNull(root.get("deletedAt")));
        }
        
        if (q != null && !q.isBlank()) {
            spec = spec.and(DocumentSpecifications.hasKeyword(q));
        }
        if (categorySlug != null && !categorySlug.isBlank()) {
            spec = spec.and(DocumentSpecifications.hasCategory(categorySlug));
        }
        if (documentType != null && !documentType.isBlank()) {
            spec = spec.and(DocumentSpecifications.hasDocumentType(documentType));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and(DocumentSpecifications.hasStatus(status));
        }
        if (year != null) {
            spec = spec.and(DocumentSpecifications.hasYear(year));
        }
        
        return documentRepository.findAll(spec, pageable).map(documentMapper::toDocumentAdminDto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public DocumentAdminDto createDocument(DocumentAdminRequest request) {
        Document document = documentMapper.toDocument(request);

        String normalized = request.getDocumentNumber().toUpperCase().replace("Đ", "D");
        document.setDocumentNumberNormalized(normalized);

        if (request.getCategoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            document.setCategories(categories);
        }

        if (request.getTagIds() != null) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            document.setTags(tags);
        }

        document.setViewCount(0);

        return documentMapper.toDocumentAdminDto(documentRepository.save(document));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public DocumentAdminDto updateDocument(Long id, DocumentAdminRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        
        documentMapper.updateDocument(document, request);
        
        String normalized = request.getDocumentNumber().toUpperCase().replace("Đ", "D");
        document.setDocumentNumberNormalized(normalized);
        
        if (request.getCategoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            document.setCategories(categories);
        }
        
        if (request.getTagIds() != null) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            document.setTags(tags);
        }
        
        return documentMapper.toDocumentAdminDto(documentRepository.save(document));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void softDeleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        
        document.setDeletedAt(LocalDateTime.now());
        documentRepository.save(document);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void restoreDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        
        document.setDeletedAt(null);
        documentRepository.save(document);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UploadPdfResponse uploadPdf(Long documentId, MultipartFile file) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        String folderPath = "construction_legal_lookup/documents/" + documentId;

        Map<?,?> result = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "use_filename", true,
                        "unique_filename", true,
                        "folder", folderPath,
                        "type", "upload",
                        "resource_type", "raw"
                ));

        if(result.get("asset_id") == null || result.get("secure_url") == null){
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }

        String pdfUrl = (String) result.get("secure_url");
        String pdfFileName = (String) result.get("display_name");
        Long pdfSizeBytes = ((Number) result.get("bytes")).longValue();

        // Extract text from PDF
        String extractedText = pdfExtractionService.extractText(file.getInputStream());
        document.setPdfUrl(pdfUrl);
        document.setPdfFileName(pdfFileName);
        document.setPdfSizeBytes(pdfSizeBytes);
        document.setContentText(extractedText);
        // Also set searchText (normalized, no accents) for search, we can implement normalization later
        document.setSearchText(extractedText);

        documentRepository.save(document);

        return UploadPdfResponse.builder()
                .documentId(documentId)
                .pdfUrl(pdfUrl)
                .pdfFileName(pdfFileName)
                .pdfSizeBytes(pdfSizeBytes)
                .contentTextExtracted(true)
                .contentTextLength(extractedText.length())
                .build();
    }
}
