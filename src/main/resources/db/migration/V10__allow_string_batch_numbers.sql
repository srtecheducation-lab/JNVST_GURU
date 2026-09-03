ALTER TABLE application.paper_questions
    ALTER COLUMN batch_no TYPE VARCHAR(60)
    USING batch_no::VARCHAR;
