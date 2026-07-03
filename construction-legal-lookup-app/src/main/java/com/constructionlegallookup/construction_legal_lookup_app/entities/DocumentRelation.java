package com.constructionlegallookup.construction_legal_lookup_app.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.constructionlegallookup.construction_legal_lookup_app.enums.RelationType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "document_relations", uniqueConstraints = @UniqueConstraint(name = "uk_relations_unique", columnNames = {"source_document_id", "target_document_id", "relation_type"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id", nullable = false)
    Document sourceDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_document_id", nullable = false)
    Document targetDocument;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 30)
    RelationType relationType;

    @Column(length = 500)
    String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    LocalDateTime createdAt;
}
