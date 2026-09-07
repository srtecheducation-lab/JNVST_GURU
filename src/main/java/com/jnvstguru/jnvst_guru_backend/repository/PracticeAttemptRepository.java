package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PracticeAttemptRepository extends JpaRepository<PracticeAttemptEntity, Long> {
    @Query("""
            select distinct a.pageNumber
            from PracticeAttemptEntity a
            where a.user = :user
              and a.practiceMode = :mode
              and a.subject = :subject
              and ((:topic is null and a.topic is null) or a.topic = :topic)
              and a.difficulty = :difficulty
            """)
    List<Integer> findCompletedPages(
            @Param("user") UserEntity user,
            @Param("mode") PracticeMode mode,
            @Param("subject") PracticeSubject subject,
            @Param("topic") ArithmeticQuestionEnums.QuestionType topic,
            @Param("difficulty") ArithmeticQuestionEnums.Difficulty difficulty);

    PracticeAttemptEntity findFirstByUserAndPracticeModeAndSubjectAndTopicAndDifficultyAndPageNumberOrderBySubmittedAtDescIdDesc(
            UserEntity user, PracticeMode practiceMode, PracticeSubject subject,
            ArithmeticQuestionEnums.QuestionType topic,
            ArithmeticQuestionEnums.Difficulty difficulty, Integer pageNumber);
}
