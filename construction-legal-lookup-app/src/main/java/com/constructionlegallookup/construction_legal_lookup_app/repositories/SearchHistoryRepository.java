package com.constructionlegallookup.construction_legal_lookup_app.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.constructionlegallookup.construction_legal_lookup_app.entities.SearchHistory;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<SearchHistory> findByUserId(Long userId, Pageable pageable);
    void deleteByUserId(Long userId);
    boolean existsByUserIdAndQueryAndCreatedAtAfter(Long userId, String query, LocalDateTime createdAt);

    @Query("SELECT s.query, COUNT(s) AS cnt FROM SearchHistory s " +
           "WHERE s.createdAt >= :startDate " +
           "AND s.query IS NOT NULL AND s.query <> '' " +
           "GROUP BY s.query " +
           "ORDER BY cnt DESC")
    List<Object[]> findTopKeywords(@Param("startDate") LocalDateTime startDate, Pageable pageable);
}
