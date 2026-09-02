# JNVST GURU Backend

## Update log
- 2026-09-02: Updated the docs to reflect the working Supabase JWT flow, Student Profile API behavior, normalized state/district data, arithmetic CRUD/filter support, and the Flyway-backed question-model additions.
- 2026-09-02: Confirmed the current backend status after the Flyway migration sequence was validated and the app built successfully.
- 2026-08-30: Expanded project overview, clarified local setup steps, and added cross-links to the documentation set so the backend status and onboarding notes stay easy to track.

Status: Backend foundation, Supabase JWT authentication, Student Profile APIs, state/district reference data, arithmetic question CRUD, and the database-layer MAT/language/paper model are in place. The public API remains intentionally limited to the implemented modules.

## Overview
JNVST GURU backend is the server-side foundation for the JNVST GURU educational platform. The repository is intentionally structured as a clean Spring Boot foundation so additional learning, assessment, and student-tracking modules can be added without reworking the platform architecture later.

The current scope remains intentionally narrow. It establishes the base application lifecycle, environment configuration, secure authenticated access flow, and the specific schema/API pieces that were already implemented and validated.

## Current implementation
- Java 25 + Spring Boot application scaffold
- Maven Wrapper-based build setup
- PostgreSQL + Supabase-ready environment configuration
- Spring Security JWT resource server using Supabase Auth
- Protected user endpoint: `GET /api/v1/me`
- Student Profile endpoints: `GET /api/v1/student-profiles/me`, `POST /api/v1/student-profiles`
- State/district reference APIs: `GET /api/v1/reference/states`, `GET /api/v1/reference/states/{stateId}/districts`
- Arithmetic Question Bank CRUD and filter endpoints under `/api/v1/arithmetic-questions`
- Flyway schema support for `application.states`, `application.districts`, `application.questions`, `application.mat_questions`, `application.language_passages`, `application.language_questions`, `application.papers`, and `application.paper_questions`
- Minimal REST health endpoint: `GET /api/v1/health`
- Automated application and endpoint tests
- Documentation structure under the `docs/` folder

## Project goals
- Provide a reliable backend base for the JNVST GURU platform
- Keep the codebase understandable and modular from the start
- Support local PostgreSQL-based development without requiring cloud services
- Define a stable API and architecture before domain-specific features grow

## Quick start
1. Ensure Java 25 is installed and `JAVA_HOME` points to the correct JDK.
2. Start PostgreSQL locally:
   ```bash
   docker compose up -d postgres
   ```
3. Run the test suite:
   ```bash
   ./mvnw clean test
   ```
4. Start the app in dev mode:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

## Environment variables
```bash
export DB_URL=jdbc:postgresql://localhost:5432/jnvst_guru_backend
export DB_USERNAME=jnvst_guru
export DB_PASSWORD=jnvst_guru_password
```

## Documentation map
- [docs/HELP.md](docs/HELP.md) — onboarding guide and local setup instructions
- [docs/CHANGELOG.md](docs/CHANGELOG.md) — documentation change log and milestone tracking
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — architectural direction and boundaries
- [docs/API.md](docs/API.md) — API contract and endpoint planning
- [docs/DATABASE.md](docs/DATABASE.md) — database direction and schema planning
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) — development workflow and prerequisites
- [docs/DECISIONS.md](docs/DECISIONS.md) — major architectural decisions

## Current API
- Subscription history endpoint: `GET /api/v1/subscriptions` (authenticated)
- Student Profile endpoints: `GET /api/v1/student-profiles/me`, `POST /api/v1/student-profiles`
- Reference data endpoints: `GET /api/v1/reference/states`, `GET /api/v1/reference/states/{stateId}/districts`
- Arithmetic Question Bank endpoints under `/api/v1/arithmetic-questions`
- Health endpoint: `GET /api/v1/health`
- Authenticated current-user endpoint: `GET /api/v1/me`
  - Requires `Authorization: Bearer <supabase-access-token>`
  - Returns the authenticated user record and profile information for the matching app user

## Important notes
- Supabase Auth is the external identity provider.
- Spring Boot validates Supabase-issued JWTs as a resource server.
- The backend uses the JWT subject to map to the application user and role records.
- The question-bank and arithmetic domain are now implemented for the initial admin CRUD and filtering flow.
- Student profile and reference data are implemented and validated, while the broader question-bank expansion remains intentionally staged.

## Repository status
This repository is in the foundation stage and should be updated whenever a significant milestone is reached. Documentation is intentionally kept in the `docs/` folder so the implementation, architecture, and operational guidance stay consistent.
