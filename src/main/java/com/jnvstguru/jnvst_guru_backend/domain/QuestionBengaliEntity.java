package com.jnvstguru.jnvst_guru_backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "question_bengali", schema = "application")
public class QuestionBengaliEntity {
    @Id @Column(name = "question_id") private Long questionId;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @MapsId
    @JoinColumn(name = "question_id", referencedColumnName = "id") private QuestionEntity question;
    @Column(columnDefinition = "TEXT") private String questionText;
    @Column(name = "option_a", columnDefinition = "TEXT") private String optionA;
    @Column(name = "option_b", columnDefinition = "TEXT") private String optionB;
    @Column(name = "option_c", columnDefinition = "TEXT") private String optionC;
    @Column(name = "option_d", columnDefinition = "TEXT") private String optionD;
    @Column(columnDefinition = "TEXT") private String explanation;
    @Column(nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(nullable = false) private OffsetDateTime updatedAt;
    @PrePersist void prePersist() { var now = OffsetDateTime.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
