CREATE TABLE application.question_english (
    question_id BIGINT PRIMARY KEY,
    question_text TEXT,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    explanation TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_question_english_question
        FOREIGN KEY (question_id) REFERENCES application.questions(id)
);

CREATE TABLE application.question_hindi (
    question_id BIGINT PRIMARY KEY,
    question_text TEXT,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    explanation TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_question_hindi_question
        FOREIGN KEY (question_id) REFERENCES application.questions(id)
);

CREATE TABLE application.question_bengali (
    question_id BIGINT PRIMARY KEY,
    question_text TEXT,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    explanation TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_question_bengali_question
        FOREIGN KEY (question_id) REFERENCES application.questions(id)
);

INSERT INTO application.question_english
    (question_id, question_text, option_a, option_b, option_c, option_d, explanation,
     created_at, updated_at)
SELECT question_id, question_text, option_a, option_b, option_c, option_d, explanation,
       created_at, updated_at
FROM application.arithmetic_questions
ON CONFLICT (question_id) DO NOTHING;

INSERT INTO application.question_english
    (question_id, question_text, option_a, option_b, option_c, option_d, explanation,
     created_at, updated_at)
SELECT question_id, question_text, option_a, option_b, option_c, option_d, explanation,
       created_at, updated_at
FROM application.language_questions
ON CONFLICT (question_id) DO NOTHING;

CREATE TABLE application.language_passages_english (
    id BIGSERIAL PRIMARY KEY,
    passage_text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE application.language_passages_hindi (
    id BIGSERIAL PRIMARY KEY,
    passage_text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE application.language_passages_bengali (
    id BIGSERIAL PRIMARY KEY,
    passage_text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO application.language_passages_english (id, passage_text, created_at, updated_at)
SELECT id, passage_text, created_at, updated_at
FROM application.language_passages;

SELECT setval(
    pg_get_serial_sequence('application.language_passages_english', 'id'),
    COALESCE((SELECT MAX(id) FROM application.language_passages_english), 1),
    (SELECT COUNT(*) > 0 FROM application.language_passages_english)
);

CREATE TABLE application.language_question_english (
    question_id BIGINT PRIMARY KEY,
    passage_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_language_question_english_question
        FOREIGN KEY (question_id) REFERENCES application.questions(id),
    CONSTRAINT fk_language_question_english_passage
        FOREIGN KEY (passage_id) REFERENCES application.language_passages_english(id)
);

CREATE TABLE application.language_question_hindi (
    question_id BIGINT PRIMARY KEY,
    passage_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_language_question_hindi_question
        FOREIGN KEY (question_id) REFERENCES application.questions(id),
    CONSTRAINT fk_language_question_hindi_passage
        FOREIGN KEY (passage_id) REFERENCES application.language_passages_hindi(id)
);

CREATE TABLE application.language_question_bengali (
    question_id BIGINT PRIMARY KEY,
    passage_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_language_question_bengali_question
        FOREIGN KEY (question_id) REFERENCES application.questions(id),
    CONSTRAINT fk_language_question_bengali_passage
        FOREIGN KEY (passage_id) REFERENCES application.language_passages_bengali(id)
);

INSERT INTO application.language_question_english (question_id, passage_id, created_at, updated_at)
SELECT question_id, passage_id, created_at, updated_at
FROM application.language_questions;

ALTER TABLE application.arithmetic_questions
    DROP COLUMN question_text,
    DROP COLUMN option_a,
    DROP COLUMN option_b,
    DROP COLUMN option_c,
    DROP COLUMN option_d,
    DROP COLUMN explanation;

ALTER TABLE application.language_questions
    DROP CONSTRAINT IF EXISTS fk_language_questions_passage,
    DROP COLUMN passage_id,
    DROP COLUMN question_text,
    DROP COLUMN option_a,
    DROP COLUMN option_b,
    DROP COLUMN option_c,
    DROP COLUMN option_d;

DROP TABLE application.language_passages;

CREATE TRIGGER trg_question_english_set_updated_at
BEFORE UPDATE ON application.question_english
FOR EACH ROW EXECUTE FUNCTION application.set_updated_at();

CREATE TRIGGER trg_question_hindi_set_updated_at
BEFORE UPDATE ON application.question_hindi
FOR EACH ROW EXECUTE FUNCTION application.set_updated_at();

CREATE TRIGGER trg_question_bengali_set_updated_at
BEFORE UPDATE ON application.question_bengali
FOR EACH ROW EXECUTE FUNCTION application.set_updated_at();

CREATE TRIGGER trg_language_passages_english_set_updated_at
BEFORE UPDATE ON application.language_passages_english
FOR EACH ROW EXECUTE FUNCTION application.set_updated_at();

CREATE TRIGGER trg_language_passages_hindi_set_updated_at
BEFORE UPDATE ON application.language_passages_hindi
FOR EACH ROW EXECUTE FUNCTION application.set_updated_at();

CREATE TRIGGER trg_language_passages_bengali_set_updated_at
BEFORE UPDATE ON application.language_passages_bengali
FOR EACH ROW EXECUTE FUNCTION application.set_updated_at();

CREATE TRIGGER trg_language_question_english_set_updated_at
BEFORE UPDATE ON application.language_question_english
FOR EACH ROW EXECUTE FUNCTION application.set_updated_at();

CREATE TRIGGER trg_language_question_hindi_set_updated_at
BEFORE UPDATE ON application.language_question_hindi
FOR EACH ROW EXECUTE FUNCTION application.set_updated_at();

CREATE TRIGGER trg_language_question_bengali_set_updated_at
BEFORE UPDATE ON application.language_question_bengali
FOR EACH ROW EXECUTE FUNCTION application.set_updated_at();
