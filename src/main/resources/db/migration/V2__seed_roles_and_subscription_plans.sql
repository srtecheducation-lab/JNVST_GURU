INSERT INTO application.roles (code, name)
VALUES
    ('STUDENT', 'Student'),
    ('TEACHER', 'Teacher'),
    ('ADMIN', 'Admin')
ON CONFLICT (code) DO NOTHING;

INSERT INTO application.subscription_plans (code, name, description, duration_days, price, currency, is_active)
VALUES
    ('FREE', 'Free', 'Permanent free access', NULL, 0.00, 'INR', TRUE),
    ('PREMIUM_MONTHLY', 'Premium Monthly', 'Monthly premium plan', 30, 299.00, 'INR', TRUE),
    ('PREMIUM_YEARLY', 'Premium Yearly', 'Yearly premium plan', 365, 2499.00, 'INR', TRUE)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    duration_days = EXCLUDED.duration_days,
    price = EXCLUDED.price,
    currency = EXCLUDED.currency,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();
