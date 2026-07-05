package com.constructionlegallookup.construction_legal_lookup_app.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.DocumentAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.DocumentAdminDto;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.UploadPdfResponse;

import java.io.IOException;

public interface AdminDocumentService {
    
    Page<DocumentAdminDto> getAllDocuments(String q, String categorySlug, String documentType, 
                                            String status, Integer year, Boolean includeDeleted, Pageable pageable);
    
    DocumentAdminDto createDocument(DocumentAdminRequest request);

    DocumentAdminDto createDocumentWithPdf(DocumentAdminRequest request, MultipartFile file) throws IOException;
    
    DocumentAdminDto updateDocument(Long id, DocumentAdminRequest request);
    
    void softDeleteDocument(Long id);
    
    void restoreDocument(Long id);

    UploadPdfResponse uploadPdf(Long documentId, MultipartFile file) throws IOException;
}
