ALTER TABLE application.practice_attempts
    DROP CONSTRAINT IF EXISTS practice_attempts_subject_check;

ALTER TABLE application.practice_attempts
    ADD CONSTRAINT chk_practice_attempts_subject
    CHECK (subject IN ('ARITHMETIC', 'MAT'));

ALTER TABLE application.practice_attempts
    ADD COLUMN topic_id BIGINT;

ALTER TABLE application.practice_attempts
    DROP CONSTRAINT IF EXISTS chk_practice_attempts_topic;

ALTER TABLE application.practice_attempts
    ADD CONSTRAINT chk_practice_attempts_topic
    CHECK (
        (subject = 'ARITHMETIC' AND
            ((practice_mode = 'TOPIC' AND topic IS NOT NULL AND topic_id IS NULL)
             OR (practice_mode = 'SUBJECT' AND topic IS NULL AND topic_id IS NULL)))
        OR
        (subject = 'MAT' AND practice_mode = 'TOPIC' AND topic IS NULL AND topic_id IS NOT NULL)
    );

ALTER TABLE application.practice_attempt_answers
    ALTER COLUMN question_id DROP NOT NULL;

ALTER TABLE application.practice_attempt_answers
    ADD COLUMN mat_question_id BIGINT,
    ADD COLUMN question_source VARCHAR(20) NOT NULL DEFAULT 'ARITHMETIC';

ALTER TABLE application.practice_attempt_answers
    DROP CONSTRAINT IF EXISTS fk_practice_attempt_answers_question;

ALTER TABLE application.practice_attempt_answers
    ADD CONSTRAINT fk_practice_attempt_answers_question
        FOREIGN KEY (question_id) REFERENCES application.questions(id),
    ADD CONSTRAINT fk_practice_attempt_answers_mat_question
        FOREIGN KEY (mat_question_id) REFERENCES application.mat_questions(id),
    ADD CONSTRAINT chk_practice_attempt_answers_source
        CHECK (
            (question_source = 'ARITHMETIC' AND question_id IS NOT NULL AND mat_question_id IS NULL)
            OR (question_source = 'MAT' AND question_id IS NULL AND mat_question_id IS NOT NULL)
        );

ALTER TABLE application.practice_attempt_answers
    ADD CONSTRAINT uq_practice_attempt_answers_attempt_mat_question
        UNIQUE (attempt_id, mat_question_id);
