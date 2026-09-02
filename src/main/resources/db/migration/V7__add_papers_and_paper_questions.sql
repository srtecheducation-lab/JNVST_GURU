CREATE TABLE IF NOT EXISTS application.papers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    exam_year INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_papers_set_updated_at
BEFORE UPDATE ON application.papers
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

CREATE TABLE IF NOT EXISTS application.paper_questions (
    id BIGSERIAL PRIMARY KEY,
    paper_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    batch_no INTEGER NOT NULL,
    question_number INTEGER NOT NULL,
    question_type VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_paper_questions_paper
        FOREIGN KEY (paper_id) REFERENCES application.papers(id),
    CONSTRAINT fk_paper_questions_question
        FOREIGN KEY (question_id) REFERENCES application.questions(id),
    CONSTRAINT uq_paper_questions_paper_number
        UNIQUE (paper_id, question_number)
);

CREATE TRIGGER trg_paper_questions_set_updated_at
BEFORE UPDATE ON application.paper_questions
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

CREATE INDEX IF NOT EXISTS idx_paper_questions_question_id
    ON application.paper_questions (question_id);

CREATE INDEX IF NOT EXISTS idx_paper_questions_batch_no
    ON application.paper_questions (batch_no);
