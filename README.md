# JNVST GURU Backend

## Update log
- 2026-09-02: Verified the working Supabase JWT flow, completed the Student Profile and reference-data APIs, and added the initial Arithmetic Question Bank backend.
- 2026-09-02: Updated the docs to reflect the live Student Profile contract, state/district normalization, and arithmetic CRUD/filter endpoints.
- 2026-08-30: Expanded project overview, clarified local setup steps, and added cross-links to the documentation set so the backend status and onboarding notes stay easy to track.

Status: Backend foundation, auth, Student Profile, state/district master data, and the initial arithmetic question bank are implemented and validated.

## Overview
JNVST GURU backend is the server-side foundation for the JNVST GURU educational platform. The repository is intentionally structured as a clean Spring Boot foundation so additional learning, assessment, and student-tracking modules can be added without reworking the platform architecture later.

The current scope is intentionally limited. It establishes the base application lifecycle, environment configuration, and startup contract before the more complex question-bank and learning-domain features are introduced.

## Current implementation
- Java 25 + Spring Boot application scaffold
- Maven Wrapper-based build setup
- PostgreSQL + Supabase-ready environment configuration
- Spring Security JWT resource server using Supabase Auth
- Protected user endpoint: `GET /api/v1/me`
- Student Profile endpoints: `GET /api/v1/student-profiles/me`, `POST /api/v1/student-profiles`
- State/district reference APIs: `GET /api/v1/reference/states`, `GET /api/v1/reference/states/{stateId}/districts`
- Arithmetic Question Bank CRUD and filter endpoints under `/api/v1/arithmetic-questions`
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
