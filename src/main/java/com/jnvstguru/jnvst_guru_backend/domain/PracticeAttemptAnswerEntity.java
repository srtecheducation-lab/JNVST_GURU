package com.jnvstguru.jnvst_guru_backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "practice_attempt_answers", schema = "application")
public class PracticeAttemptAnswerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private PracticeAttemptEntity attempt;
    @Column(name = "question_id", nullable = false)
    private Long questionId;
    @Column(name = "selected_option", length = 1)
    private String selectedOption;
    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption;
    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    public Long getId() { return id; }
    public PracticeAttemptEntity getAttempt() { return attempt; }
    public void setAttempt(PracticeAttemptEntity attempt) { this.attempt = attempt; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getSelectedOption() { return selectedOption; }
    public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
