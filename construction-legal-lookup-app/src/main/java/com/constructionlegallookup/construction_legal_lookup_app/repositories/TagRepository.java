package com.constructionlegallookup.construction_legal_lookup_app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.constructionlegallookup.construction_legal_lookup_app.entities.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findBySlug(String slug);
}
