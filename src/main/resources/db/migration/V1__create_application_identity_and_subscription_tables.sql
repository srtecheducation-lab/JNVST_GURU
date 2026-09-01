CREATE SCHEMA IF NOT EXISTS application;

CREATE OR REPLACE FUNCTION application.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS application.roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE CHECK (code IN ('STUDENT', 'TEACHER', 'ADMIN')),
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_roles_set_updated_at
BEFORE UPDATE ON application.roles
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

CREATE TABLE IF NOT EXISTS application.users (
    id BIGSERIAL PRIMARY KEY,
    auth_user_id UUID NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_users_auth_users
        FOREIGN KEY (auth_user_id) REFERENCES auth.users(id)
);

CREATE TRIGGER trg_users_set_updated_at
BEFORE UPDATE ON application.users
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

CREATE TABLE IF NOT EXISTS application.user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES application.users(id),
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES application.roles(id)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_role_id
    ON application.user_roles (role_id);

CREATE TABLE IF NOT EXISTS application.student_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    class_level VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_student_profiles_user
        FOREIGN KEY (user_id) REFERENCES application.users(id)
);

CREATE TRIGGER trg_student_profiles_set_updated_at
BEFORE UPDATE ON application.student_profiles
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

CREATE TABLE IF NOT EXISTS application.subscription_plans (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE CHECK (code IN ('FREE', 'PREMIUM_MONTHLY', 'PREMIUM_YEARLY')),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    duration_days INTEGER NULL CHECK (duration_days IS NULL OR duration_days > 0),
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    currency CHAR(3) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_subscription_plans_set_updated_at
BEFORE UPDATE ON application.subscription_plans
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

CREATE TABLE IF NOT EXISTS application.subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING', 'ACTIVE', 'EXPIRED', 'CANCELLED')),
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_subscriptions_user
        FOREIGN KEY (user_id) REFERENCES application.users(id),
    CONSTRAINT fk_subscriptions_plan
        FOREIGN KEY (plan_id) REFERENCES application.subscription_plans(id),
    CONSTRAINT chk_subscriptions_dates
        CHECK (end_at IS NULL OR end_at >= start_at)
);

CREATE TRIGGER trg_subscriptions_set_updated_at
BEFORE UPDATE ON application.subscriptions
FOR EACH ROW
EXECUTE FUNCTION application.set_updated_at();

CREATE INDEX IF NOT EXISTS idx_users_status
    ON application.users (status);

CREATE INDEX IF NOT EXISTS idx_student_profiles_user_id
    ON application.student_profiles (user_id);

CREATE INDEX IF NOT EXISTS idx_subscription_plans_is_active
    ON application.subscription_plans (is_active);

CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id
    ON application.subscriptions (user_id);

CREATE INDEX IF NOT EXISTS idx_subscriptions_plan_id
    ON application.subscriptions (plan_id);

CREATE INDEX IF NOT EXISTS idx_subscriptions_status
    ON application.subscriptions (status);
