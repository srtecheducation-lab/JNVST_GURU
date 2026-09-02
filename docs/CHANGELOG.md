# Documentation change log

This file records the documentation updates made while building the JNVST GURU backend foundation. Every significant change should be reflected here and in the relevant project markdown file.

## 2026-09-02
- Verified the working Supabase JWT flow with a real access token and confirmed the `/api/v1/me` endpoint returns the authenticated current user.
- Simplified the backend configuration to a single real database and Supabase JWT setup instead of alternating between H2/test and real DB modes.
- Confirmed the app loads `local.properties` through `spring.config.import` and validates Supabase access tokens using the configured issuer and JWKS URL.
- Added request and auth troubleshooting logs, then removed the debug noise once the real JWT flow was validated.
- Finished the Student Profile API work: 201 Created on create, duplicate-profile conflict handling, and strict JWT-sub-based user resolution.
- Implemented normalized State/District master data and added the `stateId` / `districtId` contract for student profiles.
- Fixed the LazyInitializationException in the profile GET path by fetching required relations within the transactional service layer.
- Added the initial Arithmetic Question Bank backend with CRUD, filtering, and status-based soft-delete handling.
- Added the Flyway-backed database layer for question identity, MAT questions, language passages/questions, and paper metadata.
- Updated the documentation set to reflect the live authentication, profile, reference-data, arithmetic API contract, and the current database-only MAT/language/paper scope.
- Added the authenticated subscriptions endpoint to the README, onboarding guide, and API contract, and removed stale bootstrap guidance.
- Added simple CRUD repositories for the finalized question-bank JPA entities without changing migrations or schema.

## 2026-08-30
- Created a descriptive onboarding guide in [HELP.md](HELP.md) and moved the generated help content into the docs folder.
- Expanded [README.md](../README.md) with a clearer project overview, quick-start steps, and documentation map.
- Added cross-links across the architecture, API, database, development, and decision docs to make the documentation easier to navigate.
- Added an update log section to each Markdown file so recent changes are visible at a glance.
- Replaced the placeholder database notes with the full PostgreSQL design for users, subscriptions, multilingual academic content, question bank, practice, mock tests, and future payment planning.
- Added a migration-readiness review that calls out the main schema adjustments needed before the first Flyway migration is created.
- Updated the database and architecture docs to use Supabase Auth as the authentication provider, keeping passwords and credential storage outside the application database.
- Finalized the auth/user model to separate `auth.users` from `application.users`, keep the `application` schema isolated, and clarify that plan values like `FREE` are distinct from subscription status values.
- Applied the initial Flyway foundation migration set against the Supabase DB: V1 created the application identity and subscription schema; V2 seeded the role and plan reference data.
- Updated the backend implementation plan to AUTHENTICATION OPTION A: Android handles Supabase Auth and Spring Boot acts as a JWT resource server validating Supabase-issued access tokens for application data access.

## Planned
- Add domain-specific API documentation once the question-bank and learner modules are implemented.
- Add database schema design notes when the PostgreSQL model is finalized.
- Update this changelog whenever a significant implementation or documentation milestone is reached.
