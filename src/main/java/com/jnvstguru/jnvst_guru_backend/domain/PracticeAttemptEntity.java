package com.jnvstguru.jnvst_guru_backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "practice_attempts", schema = "application")
public class PracticeAttemptEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "practice_mode", nullable = false, length = 20)
    private PracticeMode practiceMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PracticeSubject subject;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ArithmeticQuestionEnums.QuestionType topic;
    @Column(name = "topic_id")
    private Long topicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArithmeticQuestionEnums.Difficulty difficulty;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;
    @Column(name = "question_count", nullable = false)
    private Integer questionCount;
    @Column(nullable = false)
    private Integer score;
    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;
    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount;
    @Column(name = "unanswered_count", nullable = false)
    private Integer unansweredCount;
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private OffsetDateTime submittedAt;

    @PrePersist
    void prePersist() {
        if (submittedAt == null) {
            submittedAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public PracticeMode getPracticeMode() { return practiceMode; }
    public void setPracticeMode(PracticeMode practiceMode) { this.practiceMode = practiceMode; }
    public PracticeSubject getSubject() { return subject; }
    public void setSubject(PracticeSubject subject) { this.subject = subject; }
    public ArithmeticQuestionEnums.QuestionType getTopic() { return topic; }
    public void setTopic(ArithmeticQuestionEnums.QuestionType topic) { this.topic = topic; }
    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public ArithmeticQuestionEnums.Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(ArithmeticQuestionEnums.Difficulty difficulty) { this.difficulty = difficulty; }
    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }
    public Integer getWrongCount() { return wrongCount; }
    public void setWrongCount(Integer wrongCount) { this.wrongCount = wrongCount; }
    public Integer getUnansweredCount() { return unansweredCount; }
    public void setUnansweredCount(Integer unansweredCount) { this.unansweredCount = unansweredCount; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; }
}
