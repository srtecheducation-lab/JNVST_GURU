package com.jnvstguru.jnvst_guru_backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "paper_questions", schema = "application")
public class PaperQuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paper_id", nullable = false)
    private PaperEntity paper;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    @Column(name = "batch_no", nullable = false)
    private Integer batchNo;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    @Column(name = "batch_question_key", nullable = false, length = 60)
    private String batchQuestionKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 40)
    private PaperQuestionType questionType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PaperEntity getPaper() { return paper; }
    public void setPaper(PaperEntity paper) { this.paper = paper; }
    public QuestionEntity getQuestion() { return question; }
    public void setQuestion(QuestionEntity question) { this.question = question; }
    public Integer getBatchNo() { return batchNo; }
    public void setBatchNo(Integer batchNo) { this.batchNo = batchNo; }
    public Integer getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; }
    public String getBatchQuestionKey() { return batchQuestionKey; }
    public void setBatchQuestionKey(String batchQuestionKey) { this.batchQuestionKey = batchQuestionKey; }
    public PaperQuestionType getQuestionType() { return questionType; }
    public void setQuestionType(PaperQuestionType questionType) { this.questionType = questionType; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
