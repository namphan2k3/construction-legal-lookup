package com.constructionlegallookup.construction_legal_lookup_app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.constructionlegallookup.construction_legal_lookup_app.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);

    @Query("SELECT c.id, c.name, c.slug, " +
           "(SELECT COUNT(d.id) FROM Document d JOIN d.categories dc WHERE dc.id = c.id AND d.deletedAt IS NULL) " +
           "FROM Category c ORDER BY c.displayOrder ASC")
    List<Object[]> findAllCategoriesWithCount();
}
