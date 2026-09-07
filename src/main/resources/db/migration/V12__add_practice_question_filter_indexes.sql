CREATE INDEX idx_arithmetic_questions_practice_filter
    ON application.arithmetic_questions (question_type, difficulty, question_id);

CREATE INDEX idx_questions_status_id
    ON application.questions (status, id);
