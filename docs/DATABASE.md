# JNVST GURU Database Design

## Update log
- 2026-08-30: Replaced the placeholder DB notes with the complete PostgreSQL schema design, ER relationship overview, and migration-readiness review for the upcoming Flyway work.
- 2026-08-30: Finalized the approved auth/user/subscription design and executed the foundation Flyway migration set against the Supabase database. The migration history is recorded in Flyway while the application tables remain defined under the `application` schema.

## Status
- Database direction: PostgreSQL
- Migration system: Flyway
- Migration status: V1 and V2 applied successfully against the Supabase target database
- Scope: Foundation database design for user access, subscriptions, multilingual academic content, question bank, practice, and mock tests
- Payment tables: intentionally excluded from implementation for now
- Application schema: `application`
- Flyway metadata schema: `public` by default unless explicitly overridden

## Related documentation
- [README.md](../README.md) — project overview and quick start
- [docs/HELP.md](HELP.md) — onboarding and environment setup
- [ARCHITECTURE.md](ARCHITECTURE.md) — system structure and boundaries
- [API.md](API.md) — API contract expectations
- [DEVELOPMENT.md](DEVELOPMENT.md) — local setup and workflow guidance
- [DECISIONS.md](DECISIONS.md) — technical decision history
- [CHANGELOG.md](CHANGELOG.md) — documentation milestone tracking

## Core database principles
- PostgreSQL only
- Flyway migrations for every schema change
- Keep all application tables under the `application` schema; do not place them in `public`
- Use `BIGINT` primary keys consistently unless a strong reason exists otherwise
- Use `TIMESTAMP WITH TIME ZONE` for records that represent time
- Use Supabase Auth as the source of identity, credential verification, and password management
- Do not store passwords or password hashes in the application database
- Do not store phone numbers or password values in `application.users`
- Keep image binaries out of PostgreSQL; store only metadata and file paths
- Support multilingual content from day one
- Keep identity and profile data separated from the core application domain
- Keep previous-year question data in the same question infrastructure and separate it by `source_type`
- Keep subscription history rather than a plain boolean premium flag
- Keep practice flows dynamic rather than hard-coded per-practice batch/table layout

## Primary key strategy
Recommended default:
- Use `BIGINT` as the primary key type across all core tables
- Use sequence/default identity generation in PostgreSQL
- Keep join tables as composite-key tables when there is no separate business identity

This keeps the schema simpler for Java/JPA usage and avoids a mixed UUID/BIGINT design during the early implementation phase.

## Module overview

### 1. Language
- `languages`

### 2. User and access
- `users`
- `roles`
- `user_roles`
- `student_profiles`

### 3. Subscription
- `subscription_plans`
- `subscriptions`

### 4. Academic content
- `subjects`
- `subject_translations`
- `topics`
- `topic_translations`

### 5. Passages
- `passages`
- `passage_translations`

### 6. Question bank
- `exam_papers`
- `questions`
- `question_translations`
- `options`
- `option_translations`

### 7. Media
- `media_assets`
- `question_media`
- `option_media`

### 8. Practice
- `practice_sessions`
- `practice_session_questions`
- `practice_answers`

### 9. Mock test
- `mock_tests`
- `mock_test_questions`
- `mock_test_attempts`

### 10. Future payments
- `payments` (documented only, not implemented yet)

## Table design

### languages
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| code | VARCHAR(10) | Unique; e.g. `en`, `hi`, `mr` |
| name | VARCHAR(50) | Example: English |
| is_active | BOOLEAN | Default true |
| created_at | TIMESTAMPTZ | Audit |
| updated_at | TIMESTAMPTZ | Audit |

Initial values:
- `en` = English
- `hi` = Hindi
- `mr` = Marathi

### Authentication decision
JNVST GURU uses Supabase Auth as the authentication provider. The application database must not store passwords or password hashes. Authentication identity and credential management live in Supabase Auth through `auth.users`.

This project will use phone number + password authentication initially, without OTP or SMS workflow integration. This can be extended later if required, but it is not part of the initial design.

### users
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| auth_user_id | UUID UNIQUE NOT NULL | References `auth.users.id` if the database supports it safely |
| status | VARCHAR(30) NOT NULL | e.g. ACTIVE, INACTIVE, LOCKED |
| created_at | TIMESTAMPTZ | Audit |
| updated_at | TIMESTAMPTZ | Audit |

Important:
- `application.users` is the local application record for a Supabase-authenticated user
- `auth_user_id` is the link to `auth.users.id` in Supabase Auth
- `application.users` does not store phone numbers, passwords, or password hashes
- `application.users.id` is not the same as any student identifier
- `users.status` is an application user status, not a subscription plan status
- Do not add `users.is_premium`
- Do not put `student_id` in `users`

### roles
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| code | VARCHAR(50) UNIQUE NOT NULL | `STUDENT`, `TEACHER`, `ADMIN` |
| name | VARCHAR(100) NOT NULL | Display name |
| created_at | TIMESTAMPTZ | Optional audit |
| updated_at | TIMESTAMPTZ | Optional audit |

Initial roles:
- `STUDENT`
- `TEACHER`
- `ADMIN`

### user_roles
| Column | Type | Notes |
| --- | --- | --- |
| user_id | BIGINT NOT NULL FK -> application.users.id | Part of composite PK |
| role_id | BIGINT NOT NULL FK -> application.roles.id | Part of composite PK |

Primary key:
- `(user_id, role_id)`

This allows one user to have multiple roles.

### student_profiles
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| user_id | BIGINT UNIQUE NOT NULL FK -> application.users.id | One profile per user |
| name | VARCHAR(150) | Student display name |
| class_level | VARCHAR(50) | e.g. 5, 8, 10 |
| created_at | TIMESTAMPTZ | Audit |
| updated_at | TIMESTAMPTZ | Audit |

Important:
- one application user can have one student profile
- future teacher/admin profile tables are intentionally not created yet

### subscription_plans
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| code | VARCHAR(50) UNIQUE NOT NULL | FREE, PREMIUM_MONTHLY, PREMIUM_YEARLY |
| name | VARCHAR(100) NOT NULL | Display name |
| description | TEXT | Nullable |
| duration_days | INTEGER | Number of days |
| price | NUMERIC(10,2) | Amount |
| currency | CHAR(3) | e.g. INR |
| is_active | BOOLEAN | Default true |
| created_at | TIMESTAMPTZ | Audit |
| updated_at | TIMESTAMPTZ | Audit |

Initial plans:
- `FREE`
- `PREMIUM_MONTHLY`
- `PREMIUM_YEARLY`

### subscriptions
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| user_id | BIGINT NOT NULL FK -> application.users.id | Not null |
| plan_id | BIGINT NOT NULL FK -> application.subscription_plans.id | Not null |
| status | VARCHAR(30) NOT NULL | ACTIVE, EXPIRED, CANCELLED, etc. |
| start_at | TIMESTAMPTZ | Not null |
| end_at | TIMESTAMPTZ | Nullable |
| created_at | TIMESTAMPTZ | Audit |
| updated_at | TIMESTAMPTZ | Audit |

Important:
- `FREE` is a subscription plan, not a status value
- `FREE` plan subscriptions may have `status = ACTIVE` and `end_at = NULL`
- this is historical subscription data
- one user can have many past subscriptions
- no `users.is_premium` field
- the application keeps plan history separately from authentication state

### subjects
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| code | VARCHAR(50) | Unique; e.g. MENTAL_ABILITY |
| is_active | BOOLEAN | Default true |
| created_at | TIMESTAMPTZ | Audit |
| updated_at | TIMESTAMPTZ | Audit |

Initial values:
- `MENTAL_ABILITY`
- `ARITHMETIC`
- `LANGUAGE`

### subject_translations
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| subject_id | BIGINT FK -> subjects.id | Not null |
| language_id | BIGINT FK -> languages.id | Not null |
| name | VARCHAR(150) | Subject name in that language |

Unique constraint:
- `(subject_id, language_id)`

### topics
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| subject_id | BIGINT FK -> subjects.id | Not null |
| parent_topic_id | BIGINT FK -> topics.id | Nullable |
| code | VARCHAR(50) | Unique within subject when desired |
| is_active | BOOLEAN | Default true |
| created_at | TIMESTAMPTZ | Audit |
| updated_at | TIMESTAMPTZ | Audit |

Important:
- `parent_topic_id` supports future hierarchical topic trees
- it should be nullable to allow top-level topics

### topic_translations
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| topic_id | BIGINT FK -> topics.id | Not null |
| language_id | BIGINT FK -> languages.id | Not null |
| name | VARCHAR(150) | Localized title |
| description | TEXT | Nullable |

Unique constraint:
- `(topic_id, language_id)`

### passages
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| subject_id | BIGINT FK -> subjects.id | Not null |
| topic_id | BIGINT FK -> topics.id | Nullable |
| created_at | TIMESTAMPTZ | Audit |
| updated_at | TIMESTAMPTZ | Audit |

### passage_translations
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| passage_id | BIGINT FK -> passages.id | Not null |
| language_id | BIGINT FK -> languages.id | Not null |
| content | TEXT | Localized passage text |

Unique constraint:
- `(passage_id, language_id)`

Important:
- a passage translation is a language-specific adaptation, not necessarily a literal translation
- all localized passages for one passage remain linked under the same `passage_id`

### exam_papers
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| exam_code | VARCHAR(60) | Business code, unique |
| class_level | VARCHAR(50) | e.g. 8, 9, 10 |
| year | INTEGER | Previous year value |
| title | VARCHAR(255) | Display title |
| is_active | BOOLEAN | Default true |
| created_at | TIMESTAMPTZ | Audit |
| updated_at | TIMESTAMPTZ | Audit |

This table supports previous-year question sets while staying separate from the main question bank logic.

### questions
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| subject_id | BIGINT FK -> subjects.id | Not null |
| topic_id | BIGINT FK -> topics.id | Nullable if not always mapped |
| passage_id | BIGINT FK -> passages.id | Nullable |
| question_type | VARCHAR(30) | TEXT, IMAGE, PASSAGE |
| source_type | VARCHAR(30) | PRACTICE, PREVIOUS_YEAR |
| exam_paper_id | BIGINT FK -> exam_papers.id | Nullable |
| difficulty | VARCHAR(30) | e.g. EASY, MEDIUM, HARD |
| is_active | BOOLEAN | Default true |
| created_at | TIMESTAMPTZ | Audit |
| updated_at | TIMESTAMPTZ | Audit |

Recommended constraints:
- `question_type` should use a `CHECK` constraint with allowable values
- `source_type` should use a `CHECK` constraint with allowable values
- `difficulty` should use a controlled set or check constraint

### question_translations
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| question_id | BIGINT FK -> questions.id | Not null |
| language_id | BIGINT FK -> languages.id | Not null |
| content | TEXT | Localized question prompt |
| explanation | TEXT | Localized explanation |

Unique constraint:
- `(question_id, language_id)`

Important:
- these translations are localized content, not necessarily literal translations
- explanation is language-specific as well

### options
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| question_id | BIGINT FK -> questions.id | Not null |
| option_key | VARCHAR(10) | e.g. A, B, C, D |
| display_order | INTEGER | Display order |
| is_correct | BOOLEAN | Not null |

Recommended constraints:
- unique `(question_id, option_key)`
- unique `(question_id, display_order)`

### option_translations
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| option_id | BIGINT FK -> options.id | Not null |
| language_id | BIGINT FK -> languages.id | Not null |
| content | TEXT | Localized option text |

Unique constraint:
- `(option_id, language_id)`

### media_assets
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| storage_path | VARCHAR(500) | File/object storage path |
| media_type | VARCHAR(50) | image/png, image/jpeg, etc. |
| file_size | BIGINT | File size in bytes |
| width | INTEGER | Nullable |
| height | INTEGER | Nullable |
| checksum | VARCHAR(128) | Nullable |
| created_at | TIMESTAMPTZ | Audit |

Rules:
- never store binary file content in PostgreSQL
- file metadata only

### question_media
| Column | Type | Notes |
| --- | --- | --- |
| question_id | BIGINT FK -> questions.id | Part of composite PK |
| media_id | BIGINT FK -> media_assets.id | Part of composite PK |
| display_order | INTEGER | Display ordering |

Recommended primary key:
- `(question_id, media_id)`

This supports mental ability question images as well as image-based question prompts.

### option_media
| Column | Type | Notes |
| --- | --- | --- |
| option_id | BIGINT FK -> options.id | Part of composite PK |
| media_id | BIGINT FK -> media_assets.id | Part of composite PK |
| display_order | INTEGER | Display ordering |

Recommended primary key:
- `(option_id, media_id)`

This supports image-based answer options.

### practice_sessions
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| student_id | BIGINT FK -> student_profiles.id | Not null |
| subject_id | BIGINT FK -> subjects.id | Not null |
| topic_id | BIGINT FK -> topics.id | Nullable |
| started_at | TIMESTAMPTZ | Not null |
| completed_at | TIMESTAMPTZ | Nullable |
| status | VARCHAR(30) | IN_PROGRESS, COMPLETED, ABANDONED |
| score | NUMERIC(5,2) | Optional result summary |

### practice_session_questions
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| session_id | BIGINT FK -> practice_sessions.id | Not null |
| question_id | BIGINT FK -> questions.id | Not null |
| display_order | INTEGER | Ordering inside the session |

Recommended constraints:
- unique `(session_id, question_id)`
- unique `(session_id, display_order)`

This records exactly which question was shown to the student during a practice session.

### practice_answers
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| session_question_id | BIGINT FK -> practice_session_questions.id | Not null |
| selected_option_id | BIGINT FK -> options.id | Nullable |
| is_correct | BOOLEAN | Not null |
| answered_at | TIMESTAMPTZ | Not null |

Important:
- answers are tied to the exact session question, not only the question itself
- this preserves the session-specific question flow and answer history

### mock_tests
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| title | VARCHAR(255) | Not null |
| description | TEXT | Nullable |
| class_level | VARCHAR(50) | e.g. 8, 10 |
| duration_seconds | INTEGER | Not null |
| total_questions | INTEGER | Not null |
| is_active | BOOLEAN | Default true |
| created_at | TIMESTAMPTZ | Audit |
| updated_at | TIMESTAMPTZ | Audit |

### mock_test_questions
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| mock_test_id | BIGINT FK -> mock_tests.id | Not null |
| question_id | BIGINT FK -> questions.id | Not null |
| display_order | INTEGER | Ordering within test |

Recommended constraints:
- unique `(mock_test_id, question_id)`
- unique `(mock_test_id, display_order)`

### mock_test_attempts
| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Generated identity |
| student_id | BIGINT FK -> student_profiles.id | Not null |
| mock_test_id | BIGINT FK -> mock_tests.id | Not null |
| started_at | TIMESTAMPTZ | Not null |
| completed_at | TIMESTAMPTZ | Nullable |
| score | NUMERIC(5,2) | Optional total |
| status | VARCHAR(30) | IN_PROGRESS, COMPLETED, ABANDONED |

### payments (future only)
This table is intentionally not implemented now.

| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | Future |
| user_id | BIGINT FK -> users.id | Future |
| subscription_id | BIGINT FK -> subscriptions.id | Future |
| provider | VARCHAR(50) | Future |
| provider_payment_id | VARCHAR(255) | Future |
| amount | NUMERIC(10,2) | Future |
| currency | CHAR(3) | Future |
| status | VARCHAR(30) | Future |
| paid_at | TIMESTAMPTZ | Future |
| created_at | TIMESTAMPTZ | Future |

This is explicitly excluded from current migration work.

## Proposed users/authentication relationship

```text
Supabase Auth
(auth.users)
      |
      | 1:1
      v
application.users
- id (BIGINT PK)
- auth_user_id (UUID UNIQUE NOT NULL)
- status
- created_at
- updated_at
      |
      | 1:N
      v
application.user_roles
- user_id -> application.users.id
- role_id -> application.roles.id

application.roles
- id
- code
- name

application.student_profiles
- id
- user_id -> application.users.id
- name
- class_level

application.subscriptions
- id
- user_id -> application.users.id
- plan_id -> application.subscription_plans.id
- status
- start_at
- end_at
```

This design keeps Supabase Auth as the credential owner and the application database as the profile, role, subscription, and business-data store.

## ER-style relationship overview

```text
languages
  1 ───< subject_translations
  1 ───< topic_translations
  1 ───< passage_translations
  1 ───< question_translations
  1 ───< option_translations

users
  1 ───< user_roles
  1 ───1 student_profiles
  1 ───< subscriptions

roles
  1 ───< user_roles

subscription_plans
  1 ───< subscriptions

subjects
  1 ───< subject_translations
  1 ───< topics
  1 ───< passages
  1 ───< questions

topics
  1 ───< topic_translations
  1 ───< passages
  1 ───< questions
  1 ───< topics as parent_topic_id

passages
  1 ───< passage_translations
  1 ───< questions

exam_papers
  1 ───< questions

questions
  1 ───< question_translations
  1 ───< options
  1 ───< question_media
  1 ───< practice_session_questions
  1 ───< mock_test_questions

options
  1 ───< option_translations
  1 ───< option_media
  1 ───< practice_answers

media_assets
  1 ───< question_media
  1 ───< option_media

student_profiles
  1 ───< practice_sessions
  1 ───< mock_test_attempts

practice_sessions
  1 ───< practice_session_questions

practice_session_questions
  1 ───< practice_answers

mock_tests
  1 ───< mock_test_questions
  1 ───< mock_test_attempts
```

## Indexing and constraint plan

Recommended indexes:
- `users(email)` unique
- `users(phone)` partial or normal index
- `student_profiles(user_id)` unique
- `subscriptions(user_id, status)`
- `subscription_plans(code)` unique
- `subjects(code)` unique
- `topics(subject_id, parent_topic_id)`
- `questions(subject_id, topic_id, source_type)`
- `questions(exam_paper_id)`
- `practice_sessions(student_id, status)`
- `practice_session_questions(session_id, question_id)` unique
- `mock_test_attempts(student_id, mock_test_id)`
- `passage_translations(passage_id, language_id)` unique
- `question_translations(question_id, language_id)` unique
- `option_translations(option_id, language_id)` unique

Recommended data integrity rules:
- Use foreign keys with `ON DELETE RESTRICT` unless a specific cascade rule is justified
- Use `NOT NULL` on required IDs and status values
- Use `CHECK` constraints for enumerated values such as `status`, `question_type`, and `difficulty`
- Ensure translation entries are not inserted without a valid `language_id`
- Keep `updated_at` current via application or database trigger logic

## Design review before Flyway migration

The design is strong overall, but a few adjustments are recommended before creating any SQL migration files.

### Recommended changes to make before Flyway
1. Standardize on `BIGINT` primary keys for all tables.
   - This is the simplest choice for PostgreSQL and keeps object IDs consistent across the project.

2. Make `student_id` refer to `student_profiles.id`, not `users.id`.
   - This matches the requirement that student-specific context is stored in a separate profile table.
   - `practice_sessions.student_id` and `mock_test_attempts.student_id` should point to `student_profiles.id`.

3. Use composite primary keys for join tables where there is no independent business ID.
   - `user_roles(user_id, role_id)`
   - `question_media(question_id, media_id)`
   - `option_media(option_id, media_id)`

4. Add uniqueness constraints to prevent duplicates in generated session/test structures.
   - `user_roles`: composite unique key already covers this
   - `practice_session_questions`: `(session_id, question_id)` and `(session_id, display_order)`
   - `mock_test_questions`: `(mock_test_id, question_id)` and `(mock_test_id, display_order)`

5. Confirm whether `questions.topic_id` should be nullable or required.
   - For the most flexible design, keep `topic_id` nullable because some questions may only be classified by subject or passage.
   - If the product requires strict topic classification, make it `NOT NULL` with a product decision.

6. Keep `payments` out of migration scope entirely.
   - The requirement explicitly says not to implement it yet.
   - The table should remain in the design doc only, not in the migration set.

7. Review all `status` fields and convert them to controlled values using check constraints.
   - Example values: `ACTIVE`, `INACTIVE`, `LOCKED`, `IN_PROGRESS`, `COMPLETED`, `ABANDONED`, etc.

8. Decide whether to use a separate `exam_papers` record for each paper or only a paper metadata table.
   - It is acceptable as written; it acts as a logical grouping for previous-year question collections.

### No major structural change required
The proposed schema is already suitable for Flyway as a first migration set, as long as the above adjustments are reflected in the final migration design.

## Final migration note
No SQL migrations will be created in this step. This document is the approved design review for the upcoming Flyway schema work, and the next step is to convert the reviewed model into migration files only after the points above are confirmed.
