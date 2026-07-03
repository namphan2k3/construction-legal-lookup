package com.constructionlegallookup.construction_legal_lookup_app.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.SQLRestriction;

import com.constructionlegallookup.construction_legal_lookup_app.entities.base.BaseEntity;
import com.constructionlegallookup.construction_legal_lookup_app.enums.DocumentStatus;
import com.constructionlegallookup.construction_legal_lookup_app.enums.DocumentType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "documents")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Document extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "document_number", nullable = false, length = 100)
    String documentNumber;

    @Column(name = "document_number_normalized", nullable = false, unique = true, length = 100)
    String documentNumberNormalized;

    @Column(nullable = false, length = 500)
    String title;

    @Column(name = "abstract", columnDefinition = "TEXT")
    String abstractText; // Mapping database 'abstract' column

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    DocumentType documentType;

    @Column(name = "issuing_body", length = 255)
    String issuingBody;

    @Column(length = 255)
    String signer;

    @Column(name = "issued_date")
    LocalDate issuedDate;

    @Column(name = "effective_date")
    LocalDate effectiveDate;

    @Column(name = "expiry_date")
    LocalDate expiryDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    DocumentStatus status = DocumentStatus.CON_HIEU_LUC;

    @Builder.Default
    @Column(length = 100)
    String field = "XAY_DUNG";

    @Column(name = "pdf_url", length = 500)
    String pdfUrl;

    @Column(name = "pdf_file_name", length = 255)
    String pdfFileName;

    @Column(name = "pdf_size_bytes")
    Long pdfSizeBytes;

    @Lob
    @Column(name = "content_text", columnDefinition = "LONGTEXT")
    String contentText;

    @Lob
    @Column(name = "search_text", columnDefinition = "LONGTEXT")
    String searchText;

    @Column(name = "source_url", length = 500)
    String sourceUrl;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    int viewCount = 0;

    @Builder.Default
    @Column(name = "download_count", nullable = false)
    int downloadCount = 0;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "document_categories",
        joinColumns = @JoinColumn(name = "document_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    List<Category> categories;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "document_tags",
        joinColumns = @JoinColumn(name = "document_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    List<Tag> tags;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }
}
