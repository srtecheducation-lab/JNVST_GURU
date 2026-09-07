CREATE TABLE application.practice_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    practice_mode VARCHAR(20) NOT NULL CHECK (practice_mode IN ('SUBJECT', 'TOPIC')),
    subject VARCHAR(40) NOT NULL CHECK (subject IN ('ARITHMETIC')),
    topic VARCHAR(40),
    difficulty VARCHAR(20) NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    page_number INTEGER NOT NULL CHECK (page_number >= 0),
    question_count INTEGER NOT NULL CHECK (question_count >= 0),
    score INTEGER NOT NULL CHECK (score >= 0),
    correct_count INTEGER NOT NULL CHECK (correct_count >= 0),
    wrong_count INTEGER NOT NULL CHECK (wrong_count >= 0),
    unanswered_count INTEGER NOT NULL CHECK (unanswered_count >= 0),
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_practice_attempts_user FOREIGN KEY (user_id) REFERENCES application.users(id),
    CONSTRAINT chk_practice_attempts_topic CHECK (
        (practice_mode = 'TOPIC' AND topic IS NOT NULL)
        OR (practice_mode = 'SUBJECT' AND topic IS NULL)
    )
);

CREATE INDEX idx_practice_attempts_exact_set
    ON application.practice_attempts (user_id, practice_mode, subject, topic, difficulty, page_number, submitted_at DESC);

CREATE TABLE application.practice_attempt_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option VARCHAR(1),
    correct_option VARCHAR(1) NOT NULL,
    is_correct BOOLEAN NOT NULL,
    CONSTRAINT fk_practice_attempt_answers_attempt
        FOREIGN KEY (attempt_id) REFERENCES application.practice_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_practice_attempt_answers_question
        FOREIGN KEY (question_id) REFERENCES application.questions(id),
    CONSTRAINT uq_practice_attempt_answers_attempt_question UNIQUE (attempt_id, question_id),
    CONSTRAINT chk_practice_attempt_answers_selected_option
        CHECK (selected_option IS NULL OR selected_option IN ('A', 'B', 'C', 'D')),
    CONSTRAINT chk_practice_attempt_answers_correct_option
        CHECK (correct_option IN ('A', 'B', 'C', 'D'))
);

CREATE INDEX idx_practice_attempt_answers_attempt_id
    ON application.practice_attempt_answers (attempt_id);
