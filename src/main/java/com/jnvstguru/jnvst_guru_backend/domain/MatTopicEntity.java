package com.jnvstguru.jnvst_guru_backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mat_topics", schema = "application")
public class MatTopicEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 50) private String code;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder;
    @Column(name = "is_active", nullable = false) private boolean active = true;
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatTopicTranslationEntity> translations = new ArrayList<>();
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @PrePersist void prePersist() { OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(ZoneOffset.UTC); }
    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public String getCode() { return code; }
    public void setCode(String value) { code = value; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer value) { sortOrder = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
    public List<MatTopicTranslationEntity> getTranslations() { return translations; }
}
