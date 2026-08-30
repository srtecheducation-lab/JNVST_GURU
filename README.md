# JNVST GURU Backend

Status: Backend foundation is implemented and validated, with a minimal health-check API and clear documentation for local setup.

## Overview
JNVST GURU backend is the server-side foundation for the JNVST GURU educational platform. The repository is intentionally structured as a clean Spring Boot foundation so additional learning, assessment, and student-tracking modules can be added without reworking the platform architecture later.

The current scope is intentionally limited. It establishes the base application lifecycle, environment configuration, and startup contract before the more complex question-bank and learning-domain features are introduced.

## Current implementation
- Java 25 + Spring Boot application scaffold
- Maven Wrapper-based build setup
- PostgreSQL-ready environment configuration
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
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — architectural direction and boundaries
- [docs/API.md](docs/API.md) — API contract and endpoint planning
- [docs/DATABASE.md](docs/DATABASE.md) — database direction and schema planning
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) — development workflow and prerequisites
- [docs/DECISIONS.md](docs/DECISIONS.md) — major architectural decisions

## Current API
- Health endpoint: `GET /api/v1/health`

## Important notes
- Authentication and authorization are not yet implemented.
- The question-bank schema is still planned.
- This project is intentionally kept simple while the foundation remains under active development.

## Repository status
This repository is in the foundation stage and should be updated whenever a significant milestone is reached. Documentation is intentionally kept in the `docs/` folder so the implementation, architecture, and operational guidance stay consistent.
