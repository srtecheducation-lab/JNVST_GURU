package com.jnvstguru.jnvst_guru_backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "mat_questions", schema = "application")
public class MatQuestionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "topic_id", nullable = false)
    private MatTopicEntity topic;
    @Column(name = "question_image_url", nullable = false, columnDefinition = "TEXT")
    private String questionImageUrl;
    @Column(name = "option_a_image_url", nullable = false, columnDefinition = "TEXT")
    private String optionAImageUrl;
    @Column(name = "option_b_image_url", nullable = false, columnDefinition = "TEXT")
    private String optionBImageUrl;
    @Column(name = "option_c_image_url", nullable = false, columnDefinition = "TEXT")
    private String optionCImageUrl;
    @Column(name = "option_d_image_url", nullable = false, columnDefinition = "TEXT")
    private String optionDImageUrl;
    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption;
    @Column(length = 30) private String difficulty;
    @Column(name = "is_active", nullable = false) private boolean active = true;
    @Column(name = "sort_order") private Integer sortOrder;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    @PrePersist void prePersist() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        createdAt = now; updatedAt = now;
    }
    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(ZoneOffset.UTC); }
    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public MatTopicEntity getTopic() { return topic; }
    public void setTopic(MatTopicEntity value) { topic = value; }
    public String getQuestionImageUrl() { return questionImageUrl; }
    public void setQuestionImageUrl(String value) { questionImageUrl = value; }
    public String getOptionAImageUrl() { return optionAImageUrl; }
    public void setOptionAImageUrl(String value) { optionAImageUrl = value; }
    public String getOptionBImageUrl() { return optionBImageUrl; }
    public void setOptionBImageUrl(String value) { optionBImageUrl = value; }
    public String getOptionCImageUrl() { return optionCImageUrl; }
    public void setOptionCImageUrl(String value) { optionCImageUrl = value; }
    public String getOptionDImageUrl() { return optionDImageUrl; }
    public void setOptionDImageUrl(String value) { optionDImageUrl = value; }
    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String value) { correctOption = value; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String value) { difficulty = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer value) { sortOrder = value; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
