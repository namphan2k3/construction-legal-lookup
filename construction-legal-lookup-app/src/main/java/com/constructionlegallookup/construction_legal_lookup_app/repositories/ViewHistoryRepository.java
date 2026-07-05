package com.constructionlegallookup.construction_legal_lookup_app.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.constructionlegallookup.construction_legal_lookup_app.entities.ViewHistory;

@Repository
public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {
    List<ViewHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<ViewHistory> findByUserId(Long userId, Pageable pageable);
    void deleteByUserId(Long userId);
    boolean existsByUserIdAndDocumentIdAndCreatedAtAfter(Long userId, Long documentId, LocalDateTime createdAt);
}
