package com.jnvstguru.jnvst_guru_backend.repository;

import com.jnvstguru.jnvst_guru_backend.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PracticeAttemptRepository extends JpaRepository<PracticeAttemptEntity, Long> {
    @Query(value = """
            with latest as (
                select a.*,
                       row_number() over (
                           partition by practice_mode, subject, topic, topic_id, difficulty, language_code, page_number
                           order by submitted_at desc, id desc
                       ) as rn
                from application.practice_attempts a
                where a.user_id = :userId
            )
            select count(*) as attempts,
                   coalesce(sum(question_count), 0) as questions,
                   coalesce(sum(correct_count), 0) as correct,
                   coalesce(sum(wrong_count), 0) as wrong,
                   coalesce(sum(unanswered_count), 0) as unanswered,
                   coalesce(sum(score), 0) as score
            from latest where rn = 1
            """, nativeQuery = true)
    ProgressAggregateProjection aggregateLatestProgress(@Param("userId") Long userId);

    @Query(value = """
            with latest as (
                select a.*,
                       row_number() over (
                           partition by practice_mode, subject, topic, topic_id, difficulty, language_code, page_number
                           order by submitted_at desc, id desc
                       ) as rn
                from application.practice_attempts a
                where a.user_id = :userId
            )
            select subject,
                   count(*) as attempts,
                   coalesce(sum(question_count), 0) as questions,
                   coalesce(sum(correct_count), 0) as correct,
                   coalesce(sum(wrong_count), 0) as wrong,
                   coalesce(sum(unanswered_count), 0) as unanswered,
                   coalesce(sum(score), 0) as score
            from latest
            where rn = 1
            group by subject
            """, nativeQuery = true)
    List<SubjectProgressProjection> aggregateLatestProgressBySubject(@Param("userId") Long userId);

    @Query(value = """
            with latest as (
                select a.*,
                       row_number() over (
                           partition by practice_mode, subject, topic, topic_id, difficulty, language_code, page_number
                           order by submitted_at desc, id desc
                       ) as rn
                from application.practice_attempts a
                where a.user_id = :userId
            )
            select subject, topic, topic_id,
                   count(*) as attempts,
                   coalesce(sum(question_count), 0) as questions,
                   coalesce(sum(correct_count), 0) as correct,
                   coalesce(sum(wrong_count), 0) as wrong,
                   coalesce(sum(unanswered_count), 0) as unanswered,
                   coalesce(sum(score), 0) as score
            from latest
            where rn = 1
              and (topic is not null or topic_id is not null)
            group by subject, topic, topic_id
            """, nativeQuery = true)
    List<TopicProgressProjection> aggregateLatestProgressByTopic(@Param("userId") Long userId);

    Page<PracticeAttemptEntity> findByUser_IdOrderBySubmittedAtDescIdDesc(Long userId, Pageable pageable);

    interface ProgressAggregateProjection {
        Long getAttempts();
        Long getQuestions();
        Long getCorrect();
        Long getWrong();
        Long getUnanswered();
        Long getScore();
    }

    interface SubjectProgressProjection extends ProgressAggregateProjection {
        String getSubject();
    }

    interface TopicProgressProjection extends ProgressAggregateProjection {
        String getSubject();
        String getTopic();
        Long getTopicId();
    }

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

    PracticeAttemptEntity findFirstByUserAndPracticeModeAndSubjectAndLanguageCodeAndPageNumberOrderBySubmittedAtDescIdDesc(
            UserEntity user, PracticeMode practiceMode, PracticeSubject subject, String languageCode,
            Integer pageNumber);

    @Query("""
            select distinct a.pageNumber from PracticeAttemptEntity a
            where a.user = :user and a.practiceMode = :mode and a.subject = :subject
              and a.languageCode = :languageCode
            """)
    List<Integer> findCompletedLanguagePages(@Param("user") UserEntity user,
                                             @Param("mode") PracticeMode mode,
                                             @Param("subject") PracticeSubject subject,
                                             @Param("languageCode") String languageCode);
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
