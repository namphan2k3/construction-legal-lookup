package com.constructionlegallookup.construction_legal_lookup_app.entities;

import jakarta.persistence.*;

import com.constructionlegallookup.construction_legal_lookup_app.entities.base.BaseEntity;
import com.constructionlegallookup.construction_legal_lookup_app.enums.SectionType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "document_sections")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentSection extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    DocumentSection parent;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, length = 20)
    SectionType sectionType;

    @Column(name = "number_label", length = 50)
    String numberLabel;

    @Column(length = 500)
    String title;

    @Column(columnDefinition = "TEXT")
    String content;

    @Builder.Default
    @Column(name = "order_index", nullable = false)
    int orderIndex = 0;

    @Column(name = "anchor_slug", length = 100)
    String anchorSlug;
}
