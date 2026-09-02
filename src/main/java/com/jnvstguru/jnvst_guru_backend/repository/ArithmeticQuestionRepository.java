package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.ArithmeticQuestionEntity;
import com.jnvstguru.jnvst_guru_backend.domain.ArithmeticQuestionEnums;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArithmeticQuestionRepository extends JpaRepository<ArithmeticQuestionEntity, Long> {
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
}
