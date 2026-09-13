ALTER TABLE application.language_questions
    ALTER COLUMN difficulty DROP NOT NULL;

DROP INDEX IF EXISTS application.idx_language_questions_practice_order;

CREATE INDEX idx_language_questions_practice_order
    ON application.language_questions (
        language_code, batch_no, is_active, question_number, id
    );
