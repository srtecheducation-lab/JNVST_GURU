ALTER TABLE application.practice_attempts
    DROP CONSTRAINT IF EXISTS chk_practice_attempts_topic;

ALTER TABLE application.practice_attempts
    ADD CONSTRAINT chk_practice_attempts_topic
    CHECK (
        (subject = 'ARITHMETIC' AND
            ((practice_mode = 'TOPIC' AND topic IS NOT NULL AND topic_id IS NULL)
             OR (practice_mode = 'SUBJECT' AND topic IS NULL AND topic_id IS NULL)))
        OR
        (subject = 'MAT' AND
            ((practice_mode = 'TOPIC' AND topic IS NULL AND topic_id IS NOT NULL)
             OR (practice_mode = 'SUBJECT' AND topic IS NULL AND topic_id IS NULL)))
    );
