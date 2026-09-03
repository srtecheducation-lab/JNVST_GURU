ALTER TABLE application.paper_questions
    ADD COLUMN batch_question_key VARCHAR(60);

UPDATE application.paper_questions
SET batch_question_key = batch_no::VARCHAR || '-' || question_number::VARCHAR
WHERE batch_question_key IS NULL;

ALTER TABLE application.paper_questions
    ALTER COLUMN batch_question_key SET NOT NULL;

ALTER TABLE application.paper_questions
    ADD CONSTRAINT uq_paper_questions_paper_batch_question_key
        UNIQUE (paper_id, batch_question_key);
