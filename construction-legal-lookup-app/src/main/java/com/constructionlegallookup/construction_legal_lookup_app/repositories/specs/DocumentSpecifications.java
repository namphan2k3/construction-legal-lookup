package com.constructionlegallookup.construction_legal_lookup_app.repositories.specs;

import java.time.LocalDate;

import jakarta.persistence.criteria.Join;

import org.springframework.data.jpa.domain.Specification;

import com.constructionlegallookup.construction_legal_lookup_app.entities.Category;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Document;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Tag;
import com.constructionlegallookup.construction_legal_lookup_app.enums.DocumentStatus;
import com.constructionlegallookup.construction_legal_lookup_app.enums.DocumentType;

public class DocumentSpecifications {

    public static Specification<Document> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Document> hasKeyword(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return null;
            String pattern = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("abstractText")), pattern),
                cb.like(cb.lower(root.get("searchText")), pattern)
            );
        };
    }

    public static Specification<Document> hasDocumentNumber(String documentNumber) {
        return (root, query, cb) -> {
            if (documentNumber == null || documentNumber.isBlank()) return null;
            String normalized = documentNumber.replaceAll("[^a-zA-Z0-9/\\-]", "").toLowerCase();
            return cb.like(cb.lower(root.get("documentNumberNormalized")), "%" + normalized + "%");
        };
    }

    public static Specification<Document> hasDocumentType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isBlank()) return null;
            try {
                DocumentType enumType = DocumentType.valueOf(type.toUpperCase());
                return cb.equal(root.get("documentType"), enumType);
            } catch (IllegalArgumentException e) {
                return cb.disjunction(); // Invalid type results in empty match
            }
        };
    }

    public static Specification<Document> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return null;
            try {
                DocumentStatus enumStatus = DocumentStatus.valueOf(status.toUpperCase());
                return cb.equal(root.get("status"), enumStatus);
            } catch (IllegalArgumentException e) {
                return cb.disjunction();
            }
        };
    }

    public static Specification<Document> hasIssuingBody(String issuingBody) {
        return (root, query, cb) -> {
            if (issuingBody == null || issuingBody.isBlank()) return null;
            return cb.like(cb.lower(root.get("issuingBody")), "%" + issuingBody.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Document> hasYear(Integer year) {
        return (root, query, cb) -> {
            if (year == null) return null;
            return cb.equal(cb.function("YEAR", Integer.class, root.get("issuedDate")), year);
        };
    }

    public static Specification<Document> hasCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;
            // Prevent duplicate records when fetching
            if (Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType())) {
                Join<Document, Category> join = root.join("categories");
                return cb.equal(join.get("id"), categoryId);
            } else {
                root.fetch("categories", jakarta.persistence.criteria.JoinType.INNER);
                return cb.equal(root.join("categories").get("id"), categoryId);
            }
        };
    }

    public static Specification<Document> hasCategory(String categorySlug) {
        return (root, query, cb) -> {
            if (categorySlug == null || categorySlug.isBlank()) return null;
            // Prevent duplicate records when fetching
            if (Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType())) {
                Join<Document, Category> join = root.join("categories");
                return cb.equal(join.get("slug"), categorySlug);
            } else {
                root.fetch("categories", jakarta.persistence.criteria.JoinType.INNER);
                return cb.equal(root.join("categories").get("slug"), categorySlug);
            }
        };
    }

    public static Specification<Document> hasTag(Long tagId) {
        return (root, query, cb) -> {
            if (tagId == null) return null;
            if (Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType())) {
                Join<Document, Tag> join = root.join("tags");
                return cb.equal(join.get("id"), tagId);
            } else {
                root.fetch("tags", jakarta.persistence.criteria.JoinType.INNER);
                return cb.equal(root.join("tags").get("id"), tagId);
            }
        };
    }

    public static Specification<Document> isIssuedAfter(LocalDate dateFrom) {
        return (root, query, cb) -> {
            if (dateFrom == null) return null;
            return cb.greaterThanOrEqualTo(root.get("issuedDate"), dateFrom);
        };
    }

    public static Specification<Document> isIssuedBefore(LocalDate dateTo) {
        return (root, query, cb) -> {
            if (dateTo == null) return null;
            return cb.lessThanOrEqualTo(root.get("issuedDate"), dateTo);
        };
    }
}
