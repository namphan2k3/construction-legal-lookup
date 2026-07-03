package com.constructionlegallookup.construction_legal_lookup_app.entities;

import jakarta.persistence.*;

import com.constructionlegallookup.construction_legal_lookup_app.entities.base.BaseEntity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "categories")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 150)
    String name;

    @Column(nullable = false, unique = true, length = 150)
    String slug;

    @Column(columnDefinition = "TEXT")
    String description;

    @Builder.Default
    @Column(name = "display_order", nullable = false)
    int displayOrder = 0;
}
