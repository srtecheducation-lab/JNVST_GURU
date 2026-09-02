CREATE TABLE IF NOT EXISTS application.exam_sessions (
    id BIGSERIAL PRIMARY KEY,
    exam_code VARCHAR(50) NOT NULL,
    class_level INTEGER NOT NULL CHECK (class_level > 0),
    session_name VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_exam_sessions UNIQUE (exam_code, class_level, session_name)
);

CREATE TRIGGER trg_exam_sessions_set_updated_at
BEFORE UPDATE ON application.exam_sessions
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

ALTER TABLE application.student_profiles
    ADD COLUMN IF NOT EXISTS date_of_birth DATE,
    ADD COLUMN IF NOT EXISTS gender VARCHAR(10),
    ADD COLUMN IF NOT EXISTS category VARCHAR(10),
    ADD COLUMN IF NOT EXISTS residential_area VARCHAR(10),
    ADD COLUMN IF NOT EXISTS state VARCHAR(150),
    ADD COLUMN IF NOT EXISTS district VARCHAR(150),
    ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(20),
    ADD COLUMN IF NOT EXISTS exam_session_id BIGINT;

ALTER TABLE application.student_profiles
    DROP CONSTRAINT IF EXISTS chk_student_profiles_gender;

ALTER TABLE application.student_profiles
    ADD CONSTRAINT chk_student_profiles_gender
    CHECK (gender IS NULL OR gender IN ('MALE', 'FEMALE', 'OTHER'));

ALTER TABLE application.student_profiles
    DROP CONSTRAINT IF EXISTS chk_student_profiles_category;

ALTER TABLE application.student_profiles
    ADD CONSTRAINT chk_student_profiles_category
    CHECK (category IS NULL OR category IN ('GENERAL', 'OBC', 'SC', 'ST'));

ALTER TABLE application.student_profiles
    DROP CONSTRAINT IF EXISTS chk_student_profiles_residential_area;

ALTER TABLE application.student_profiles
    ADD CONSTRAINT chk_student_profiles_residential_area
    CHECK (residential_area IS NULL OR residential_area IN ('RURAL', 'URBAN'));

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'application'
          AND table_name = 'student_profiles'
          AND column_name = 'class_level'
          AND data_type = 'character varying'
    ) THEN
        ALTER TABLE application.student_profiles
            ALTER COLUMN class_level TYPE INTEGER
            USING CASE
                WHEN class_level ~ '^[0-9]+$' THEN class_level::INTEGER
                ELSE NULL
            END;
    END IF;
END $$;

INSERT INTO application.exam_sessions (exam_code, class_level, session_name, status, created_at, updated_at)
VALUES ('JNVST', 6, '2027-28', 'ACTIVE', NOW(), NOW())
ON CONFLICT (exam_code, class_level, session_name) DO NOTHING;

UPDATE application.student_profiles sp
SET exam_session_id = es.id
FROM application.exam_sessions es
WHERE sp.exam_session_id IS NULL
  AND es.exam_code = 'JNVST'
  AND es.class_level = 6
  AND es.session_name = '2027-28';

CREATE INDEX IF NOT EXISTS idx_exam_sessions_exam_code_class_level
    ON application.exam_sessions (exam_code, class_level);

CREATE INDEX IF NOT EXISTS idx_student_profiles_exam_session_id
    ON application.student_profiles (exam_session_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'application'
          AND table_name = 'student_profiles'
          AND constraint_name = 'fk_student_profiles_exam_session'
    ) THEN
        ALTER TABLE application.student_profiles
            ADD CONSTRAINT fk_student_profiles_exam_session
            FOREIGN KEY (exam_session_id) REFERENCES application.exam_sessions(id);
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM application.student_profiles
        WHERE exam_session_id IS NULL
    ) THEN
        RAISE NOTICE 'Skipped NOT NULL on application.student_profiles.exam_session_id because existing rows still require backfill.';
    ELSE
        ALTER TABLE application.student_profiles
            ALTER COLUMN exam_session_id SET NOT NULL;
    END IF;
END $$;
