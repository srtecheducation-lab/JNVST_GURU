

CREATE TRIGGER trg_questions_set_updated_at
BEFORE UPDATE ON application.questions
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

CREATE TABLE IF NOT EXISTS application.mat_questions (
    question_id BIGINT PRIMARY KEY,
    question_text TEXT,
    correct_option VARCHAR(1) NOT NULL CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    difficulty VARCHAR(30) NOT NULL,
    explanation TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_mat_questions_question
        FOREIGN KEY (question_id) REFERENCES application.questions(id)
);

CREATE TRIGGER trg_mat_questions_set_updated_at
BEFORE UPDATE ON application.mat_questions
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

CREATE TABLE IF NOT EXISTS application.language_passages (
    id BIGSERIAL PRIMARY KEY,
    passage_text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_language_passages_set_updated_at
BEFORE UPDATE ON application.language_passages
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

CREATE TABLE IF NOT EXISTS application.language_questions (
    question_id BIGINT PRIMARY KEY,
    passage_id BIGINT,
    question_text TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option VARCHAR(1) NOT NULL CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    difficulty VARCHAR(30) NOT NULL,
    explanation TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_language_questions_question
        FOREIGN KEY (question_id) REFERENCES application.questions(id),
    CONSTRAINT fk_language_questions_passage
        FOREIGN KEY (passage_id) REFERENCES application.language_passages(id)
);

CREATE TRIGGER trg_language_questions_set_updated_at
BEFORE UPDATE ON application.language_questions
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();
