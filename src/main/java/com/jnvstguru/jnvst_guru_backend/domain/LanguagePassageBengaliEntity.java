package com.jnvstguru.jnvst_guru_backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "language_passages_bengali", schema = "application")
public class LanguagePassageBengaliEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "passage_text", nullable = false, columnDefinition = "TEXT") private String passageText;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @PrePersist void prePersist() { var now = OffsetDateTime.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
