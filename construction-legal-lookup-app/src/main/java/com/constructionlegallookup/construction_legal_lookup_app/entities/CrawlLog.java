package com.constructionlegallookup.construction_legal_lookup_app.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.constructionlegallookup.construction_legal_lookup_app.enums.CrawlStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "crawl_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CrawlLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    CrawlStatus status;

    @Column(name = "triggered_by", length = 50)
    String triggeredBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_user_id")
    User triggeredByUser;

    @Column(name = "started_at", nullable = false, updatable = false)
    @CreatedDate
    LocalDateTime startedAt;

    @Column(name = "finished_at")
    LocalDateTime finishedAt;

    @Builder.Default
    @Column(name = "inserted_count", nullable = false)
    int insertedCount = 0;

    @Builder.Default
    @Column(name = "updated_count", nullable = false)
    int updatedCount = 0;

    @Builder.Default
    @Column(name = "skipped_count", nullable = false)
    int skippedCount = 0;

    @Builder.Default
    @Column(name = "error_count", nullable = false)
    int errorCount = 0;

    @Column(name = "error_details", columnDefinition = "JSON")
    String errorDetails;

    @Column(columnDefinition = "TEXT")
    String notes;
}
