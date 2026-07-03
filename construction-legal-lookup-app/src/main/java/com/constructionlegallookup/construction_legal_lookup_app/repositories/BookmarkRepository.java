package com.constructionlegallookup.construction_legal_lookup_app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.constructionlegallookup.construction_legal_lookup_app.entities.Bookmark;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserIdAndDocumentId(Long userId, Long documentId);
    Optional<Bookmark> findByUserIdAndDocumentId(Long userId, Long documentId);
    List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Bookmark> findByUserId(Long userId, Pageable pageable);
}
