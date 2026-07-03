package com.constructionlegallookup.construction_legal_lookup_app.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.constructionlegallookup.construction_legal_lookup_app.entities.User;
import com.constructionlegallookup.construction_legal_lookup_app.enums.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndDeletedAtIsNull(Long id);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:q IS NULL OR LOWER(u.email) LIKE %:q% OR LOWER(u.fullName) LIKE %:q%) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled) AND " +
           "(:role IS NULL OR u.role = :role)")
    Page<User> findAllByFilters(@Param("q") String q, @Param("enabled") Boolean enabled, 
                                  @Param("role") UserRole role, Pageable pageable);
}
