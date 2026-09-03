package com.jnvstguru.jnvst_guru_backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "language_question_hindi", schema = "application")
public class LanguageQuestionHindiEntity {
    @Id @Column(name = "question_id") private Long questionId;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @MapsId
    @JoinColumn(name = "question_id", referencedColumnName = "id") private QuestionEntity question;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "passage_id") private LanguagePassageHindiEntity passage;
    @Column(nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(nullable = false) private OffsetDateTime updatedAt;
    @PrePersist void prePersist() { var now = OffsetDateTime.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
