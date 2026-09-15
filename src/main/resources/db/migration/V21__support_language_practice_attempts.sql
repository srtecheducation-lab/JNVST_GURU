ALTER TABLE application.practice_attempts
    ADD COLUMN language_code VARCHAR(10);

ALTER TABLE application.practice_attempts
    DROP CONSTRAINT IF EXISTS chk_practice_attempts_subject;

ALTER TABLE application.practice_attempts
    ADD CONSTRAINT chk_practice_attempts_subject
    CHECK (subject IN ('ARITHMETIC', 'MAT', 'LANGUAGE'));

ALTER TABLE application.practice_attempts
    DROP CONSTRAINT IF EXISTS chk_practice_attempts_topic;

ALTER TABLE application.practice_attempts
    ADD CONSTRAINT chk_practice_attempts_topic
    CHECK (
        (subject = 'ARITHMETIC' AND
            ((practice_mode = 'TOPIC' AND topic IS NOT NULL AND topic_id IS NULL AND language_code IS NULL)
             OR (practice_mode = 'SUBJECT' AND topic IS NULL AND topic_id IS NULL AND language_code IS NULL)))
        OR
        (subject = 'MAT' AND
            ((practice_mode = 'TOPIC' AND topic IS NULL AND topic_id IS NOT NULL AND language_code IS NULL)
             OR (practice_mode = 'SUBJECT' AND topic IS NULL AND topic_id IS NULL AND language_code IS NULL)))
        OR
        (subject = 'LANGUAGE' AND practice_mode = 'SUBJECT' AND topic IS NULL
            AND topic_id IS NULL AND language_code IN ('en', 'bn'))
    );

ALTER TABLE application.practice_attempt_answers
    ADD COLUMN language_question_id BIGINT;

ALTER TABLE application.practice_attempt_answers
    DROP CONSTRAINT IF EXISTS chk_practice_attempt_answers_source;

ALTER TABLE application.practice_attempt_answers
    ADD CONSTRAINT chk_practice_attempt_answers_source
    CHECK (
        (question_source = 'ARITHMETIC' AND question_id IS NOT NULL AND mat_question_id IS NULL AND language_question_id IS NULL)
        OR (question_source = 'MAT' AND question_id IS NULL AND mat_question_id IS NOT NULL AND language_question_id IS NULL)
        OR (question_source = 'LANGUAGE' AND question_id IS NULL AND mat_question_id IS NULL AND language_question_id IS NOT NULL)
    );

ALTER TABLE application.practice_attempt_answers
    ADD CONSTRAINT fk_practice_attempt_answers_language_question
        FOREIGN KEY (language_question_id) REFERENCES application.language_questions(id);

ALTER TABLE application.practice_attempt_answers
    ADD CONSTRAINT uq_practice_attempt_answers_attempt_language_question
        UNIQUE (attempt_id, language_question_id);
