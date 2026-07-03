package com.constructionlegallookup.construction_legal_lookup_app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.constructionlegallookup.construction_legal_lookup_app.entities.CrawlLog;

@Repository
public interface CrawlLogRepository extends JpaRepository<CrawlLog, Long> {
    List<CrawlLog> findAllByOrderByStartedAtDesc();
}
