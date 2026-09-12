package com.jnvstguru.jnvst_guru_backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "mat_topic_translations", schema = "application",
       uniqueConstraints = @UniqueConstraint(columnNames = {"topic_id", "language_code"}))
public class MatTopicTranslationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "topic_id", nullable = false)
    private MatTopicEntity topic;
    @Column(name = "language_code", nullable = false, length = 10) private String languageCode;
    @Column(nullable = false, length = 150) private String name;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @PrePersist void prePersist() { OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(ZoneOffset.UTC); }
    public void setTopic(MatTopicEntity value) { topic = value; }
    public void setLanguageCode(String value) { languageCode = value; }
    public void setName(String value) { name = value; }
    public void setDescription(String value) { description = value; }
}
