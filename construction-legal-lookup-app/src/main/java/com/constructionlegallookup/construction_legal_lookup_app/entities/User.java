package com.constructionlegallookup.construction_legal_lookup_app.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.SQLRestriction;

import com.constructionlegallookup.construction_legal_lookup_app.entities.base.BaseEntity;
import com.constructionlegallookup.construction_legal_lookup_app.enums.UserRole;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 255)
    String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    String passwordHash;

    @Column(name = "full_name", length = 150)
    String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    UserRole role;

    @Builder.Default
    @Column(nullable = false)
    boolean enabled = true;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }
}
