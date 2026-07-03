package com.constructionlegallookup.construction_legal_lookup_app.entities;

import java.util.Date;

import jakarta.persistence.*;

import com.constructionlegallookup.construction_legal_lookup_app.entities.base.BaseEntity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "invalidated_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidatedToken extends BaseEntity {
    @Id
    @Column(length = 255)
    String id;

    @Column(name = "expiry_time", nullable = false)
    Date expiryTime;
}
