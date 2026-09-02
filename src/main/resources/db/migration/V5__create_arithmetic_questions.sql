CREATE TABLE IF NOT EXISTS application.questions (
    id BIGSERIAL PRIMARY KEY,
    content_hash VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS application.arithmetic_questions (
    question_id BIGINT PRIMARY KEY,
    question_text TEXT NOT NULL,
    question_type VARCHAR(40) NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option VARCHAR(1) NOT NULL
        CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    difficulty VARCHAR(30) NOT NULL,
    explanation TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_arithmetic_questions_question
        FOREIGN KEY (question_id)
        REFERENCES application.questions(id)
);

CREATE TRIGGER trg_arithmetic_questions_set_updated_at
BEFORE UPDATE ON application.arithmetic_questions
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();
