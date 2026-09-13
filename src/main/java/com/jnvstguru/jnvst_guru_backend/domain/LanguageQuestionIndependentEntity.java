package com.jnvstguru.jnvst_guru_backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "language_questions", schema = "application",
       uniqueConstraints = @UniqueConstraint(columnNames = {"language_code", "batch_no", "question_number"}))
public class LanguageQuestionIndependentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;
    @Column(name = "batch_no", nullable = false, length = 60)
    private String batchNo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passage_id", nullable = false)
    private LanguagePassageEntity passage;
    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;
    @Column(name = "option_a", nullable = false, columnDefinition = "TEXT")
    private String optionA;
    @Column(name = "option_b", nullable = false, columnDefinition = "TEXT")
    private String optionB;
    @Column(name = "option_c", nullable = false, columnDefinition = "TEXT")
    private String optionC;
    @Column(name = "option_d", nullable = false, columnDefinition = "TEXT")
    private String optionD;
    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption;
    @Column(columnDefinition = "TEXT")
    private String explanation;
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist void prePersist() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        createdAt = now;
        updatedAt = now;
    }
    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(ZoneOffset.UTC); }
    public Long getId() { return id; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String value) { languageCode = value; }
    public void setLanguageCode(LanguageCode value) { languageCode = value == null ? null : value.code(); }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String value) { batchNo = value; }
    public LanguagePassageEntity getPassage() { return passage; }
    public void setPassage(LanguagePassageEntity value) { passage = value; }
    public Integer getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(Integer value) { questionNumber = value; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String value) { questionText = value; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String value) { optionA = value; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String value) { optionB = value; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String value) { optionC = value; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String value) { optionD = value; }
    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String value) { correctOption = value; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String value) { explanation = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
}
