ALTER TABLE application.language_passages
    DROP CONSTRAINT IF EXISTS language_passages_passage_number_check;

ALTER TABLE application.language_passages
    ADD CONSTRAINT language_passages_passage_number_positive
    CHECK (passage_number > 0);

ALTER TABLE application.language_questions
    DROP CONSTRAINT IF EXISTS language_questions_question_number_check;

ALTER TABLE application.language_questions
    ADD CONSTRAINT language_questions_question_number_positive
    CHECK (question_number > 0);
