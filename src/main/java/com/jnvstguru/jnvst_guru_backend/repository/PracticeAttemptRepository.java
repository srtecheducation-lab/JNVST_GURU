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

    @Query("""
            select distinct a.pageNumber from PracticeAttemptEntity a
            where a.user = :user and a.practiceMode = :mode and a.subject = :subject
              and a.topicId = :topicId and a.difficulty = :difficulty
            """)
    List<Integer> findCompletedMatPages(@Param("user") UserEntity user, @Param("mode") PracticeMode mode,
                                        @Param("subject") PracticeSubject subject, @Param("topicId") Long topicId,
                                        @Param("difficulty") ArithmeticQuestionEnums.Difficulty difficulty);

    @Query("""
            select distinct a.pageNumber from PracticeAttemptEntity a
            where a.user = :user and a.practiceMode = :mode and a.subject = :subject
              and a.topicId is null and a.difficulty = :difficulty
            """)
    List<Integer> findCompletedMatSubjectPages(
            @Param("user") UserEntity user, @Param("mode") PracticeMode mode,
            @Param("subject") PracticeSubject subject,
            @Param("difficulty") ArithmeticQuestionEnums.Difficulty difficulty);

    PracticeAttemptEntity findFirstByUserAndPracticeModeAndSubjectAndTopicIdAndDifficultyAndPageNumberOrderBySubmittedAtDescIdDesc(
            UserEntity user, PracticeMode practiceMode, PracticeSubject subject, Long topicId,
            ArithmeticQuestionEnums.Difficulty difficulty, Integer pageNumber);

    @Query("""
            select a from PracticeAttemptEntity a
            where a.user = :user and a.practiceMode = :mode and a.subject = :subject
              and a.topicId is null and a.difficulty = :difficulty and a.pageNumber = :page
            order by a.submittedAt desc, a.id desc
            """)
    List<PracticeAttemptEntity> findLatestMatSubject(
            @Param("user") UserEntity user, @Param("mode") PracticeMode mode,
            @Param("subject") PracticeSubject subject,
            @Param("difficulty") ArithmeticQuestionEnums.Difficulty difficulty,
            @Param("page") Integer page,
            org.springframework.data.domain.Pageable pageable);
}
