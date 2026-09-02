CREATE TABLE IF NOT EXISTS application.states (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_states_set_updated_at
BEFORE UPDATE ON application.states
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

CREATE TABLE IF NOT EXISTS application.districts (
    id BIGSERIAL PRIMARY KEY,
    state_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_districts_state
        FOREIGN KEY (state_id) REFERENCES application.states(id),
    CONSTRAINT uq_districts_state_code UNIQUE (state_id, code),
    CONSTRAINT uq_districts_state_name UNIQUE (state_id, name)
);

CREATE INDEX IF NOT EXISTS idx_districts_state_id
    ON application.districts (state_id);

CREATE TRIGGER trg_districts_set_updated_at
BEFORE UPDATE ON application.districts
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

INSERT INTO application.states (code, name, status, created_at, updated_at)
VALUES ('AS', 'Assam', 'ACTIVE', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

WITH state_row AS (
    SELECT id
    FROM application.states
    WHERE code = 'AS'
)
INSERT INTO application.districts (state_id, code, name, status, created_at, updated_at)
SELECT state_row.id, v.code, v.name, 'ACTIVE', NOW(), NOW()
FROM state_row
CROSS JOIN (VALUES
    ('BAJALI', 'Bajali'),
    ('BAKSA', 'Baksa'),
    ('BARPETA', 'Barpeta'),
    ('BISWANATH', 'Biswanath'),
    ('BONGAIGAON', 'Bongaigaon'),
    ('CACHAR', 'Cachar'),
    ('CHARAIDEO', 'Charaideo'),
    ('CHIRANG', 'Chirang'),
    ('DARRANG', 'Darrang'),
    ('DHEMAJI', 'Dhemaji'),
    ('DHUBRI', 'Dhubri'),
    ('DIBRUGARH', 'Dibrugarh'),
    ('DIMA_HASAO', 'Dima Hasao'),
    ('GOALPARA', 'Goalpara'),
    ('GOLAGHAT', 'Golaghat'),
    ('HAILAKANDI', 'Hailakandi'),
    ('HOJAI', 'Hojai'),
    ('JORHAT', 'Jorhat'),
    ('KAMRUP', 'Kamrup'),
    ('KAMRUP_METROPOLITAN', 'Kamrup Metropolitan'),
    ('KARBI_ANGLONG', 'Karbi Anglong'),
    ('KOKRAJHAR', 'Kokrajhar'),
    ('LAKHIMPUR', 'Lakhimpur'),
    ('MAJULI', 'Majuli'),
    ('MORIGAON', 'Morigaon'),
    ('NAGAON', 'Nagaon'),
    ('NALBARI', 'Nalbari'),
    ('SIVASAGAR', 'Sivasagar'),
    ('SONITPUR', 'Sonitpur'),
    ('SOUTH_SALMARA_MANKACHAR', 'South Salmara-Mankachar'),
    ('SRIBHUMI', 'Sribhumi'),
    ('TINSUKIA', 'Tinsukia'),
    ('TAMULPUR', 'Tamulpur'),
    ('UDALGURI', 'Udalguri'),
    ('WEST_KARBI_ANGLONG', 'West Karbi Anglong')
) AS v(code, name)
ON CONFLICT (state_id, code) DO NOTHING;

ALTER TABLE application.student_profiles
    ADD COLUMN IF NOT EXISTS state_id BIGINT,
    ADD COLUMN IF NOT EXISTS district_id BIGINT;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'application'
          AND table_name = 'student_profiles'
          AND column_name = 'state'
    ) THEN
        ALTER TABLE application.student_profiles
            DROP COLUMN IF EXISTS state;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'application'
          AND table_name = 'student_profiles'
          AND column_name = 'district'
    ) THEN
        ALTER TABLE application.student_profiles
            DROP COLUMN IF EXISTS district;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'application'
          AND table_name = 'student_profiles'
          AND constraint_name = 'fk_student_profiles_state'
    ) THEN
        ALTER TABLE application.student_profiles
            ADD CONSTRAINT fk_student_profiles_state
            FOREIGN KEY (state_id) REFERENCES application.states(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'application'
          AND table_name = 'student_profiles'
          AND constraint_name = 'fk_student_profiles_district'
    ) THEN
        ALTER TABLE application.student_profiles
            ADD CONSTRAINT fk_student_profiles_district
            FOREIGN KEY (district_id) REFERENCES application.districts(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_student_profiles_state_id
    ON application.student_profiles (state_id);

CREATE INDEX IF NOT EXISTS idx_student_profiles_district_id
    ON application.student_profiles (district_id);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM application.student_profiles
        WHERE state_id IS NULL OR district_id IS NULL
    ) THEN
        RAISE NOTICE 'Skipped NOT NULL on student profile location columns because application data is empty; new rows must populate state_id and district_id.';
    ELSE
        ALTER TABLE application.student_profiles
            ALTER COLUMN state_id SET NOT NULL,
            ALTER COLUMN district_id SET NOT NULL;
    END IF;
END $$;
