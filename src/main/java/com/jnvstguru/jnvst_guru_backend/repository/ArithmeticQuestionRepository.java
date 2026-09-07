package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.ArithmeticQuestionEntity;
import com.jnvstguru.jnvst_guru_backend.domain.ArithmeticQuestionEnums;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArithmeticQuestionRepository extends JpaRepository<ArithmeticQuestionEntity, Long> {
    @Query("""
            select a
            from ArithmeticQuestionEntity a
            join fetch a.question q
            where q.status = com.jnvstguru.jnvst_guru_backend.domain.QuestionStatus.ACTIVE
              and (:topic is null or a.questionType = :topic)
              and a.difficulty = :difficulty
            order by a.questionId
            """)
    List<ArithmeticQuestionEntity> findActivePracticeQuestions(
            @Param("topic") ArithmeticQuestionEnums.QuestionType topic,
            @Param("difficulty") ArithmeticQuestionEnums.Difficulty difficulty,
            Pageable pageable);

    @Query("""
            select count(a)
            from ArithmeticQuestionEntity a
            join a.question q
            where q.status = com.jnvstguru.jnvst_guru_backend.domain.QuestionStatus.ACTIVE
              and (:topic is null or a.questionType = :topic)
              and a.difficulty = :difficulty
            """)
    long countActivePracticeQuestions(
            @Param("topic") ArithmeticQuestionEnums.QuestionType topic,
            @Param("difficulty") ArithmeticQuestionEnums.Difficulty difficulty);

    @Query("""
            select a.questionId as questionId,
                   a.questionType as questionType,
                   e.questionText as questionText,
                   e.optionA as optionA,
                   e.optionB as optionB,
                   e.optionC as optionC,
                   e.optionD as optionD,
                   a.difficulty as difficulty
            from ArithmeticQuestionEntity a
            join a.question q
            join q.englishContent e
            where q.status = com.jnvstguru.jnvst_guru_backend.domain.QuestionStatus.ACTIVE
              and (:questionType is null or a.questionType = :questionType)
              and (:difficulty is null or a.difficulty = :difficulty)
            """)
    Page<StudentArithmeticQuestionProjection> findActiveStudentQuestions(
            @Param("questionType") ArithmeticQuestionEnums.QuestionType questionType,
            @Param("difficulty") ArithmeticQuestionEnums.Difficulty difficulty,
            Pageable pageable);

    Page<ArithmeticQuestionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ArithmeticQuestionEntity> findByStatusOrderByCreatedAtDesc(ArithmeticQuestionEnums.Status status, Pageable pageable);

    Page<ArithmeticQuestionEntity> findByQuestionTypeAndStatusOrderByCreatedAtDesc(
            ArithmeticQuestionEnums.QuestionType questionType,
            ArithmeticQuestionEnums.Status status,
            Pageable pageable);

    Page<ArithmeticQuestionEntity> findByDifficultyAndStatusOrderByCreatedAtDesc(
            ArithmeticQuestionEnums.Difficulty difficulty,
            ArithmeticQuestionEnums.Status status,
            Pageable pageable);

    Page<ArithmeticQuestionEntity> findByQuestionTypeAndDifficultyAndStatusOrderByCreatedAtDesc(
            ArithmeticQuestionEnums.QuestionType questionType,
            ArithmeticQuestionEnums.Difficulty difficulty,
            ArithmeticQuestionEnums.Status status,
            Pageable pageable);

    interface StudentArithmeticQuestionProjection {
        Long getQuestionId();

        ArithmeticQuestionEnums.QuestionType getQuestionType();

        String getQuestionText();

        String getOptionA();

        String getOptionB();

        String getOptionC();

        String getOptionD();

        ArithmeticQuestionEnums.Difficulty getDifficulty();
    }
}
