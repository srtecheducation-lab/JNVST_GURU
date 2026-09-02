# Development

## Update log
- 2026-09-02: Updated the dev notes to reflect the current working authentication flow, profile/reference data functionality, arithmetic CRUD work, and the Flyway schema additions for question, MAT, language, and paper tables.
- 2026-09-02: Confirmed the documented API surface includes authenticated subscriptions alongside the profile, reference-data, and arithmetic modules.
- 2026-09-02: Added the question-bank Spring Data JPA repository layer with simple CRUD interfaces only.
- 2026-09-02: Added read-only QuestionService and PaperService operations for the upcoming API layer.
- 2026-08-30: Added setup references and development workflow notes to keep local environment instructions aligned with the current backend foundation.

Status: The backend foundation is verified with Java 25 + Maven Wrapper. The active development focus includes secure auth, student profile/ref data, arithmetic question CRUD, and Flyway-backed schema support for MAT/language/paper models without exposing new public APIs prematurely.

## Related documentation
- [README.md](../README.md) — project overview and quick start
- [HELP.md](HELP.md) — onboarding guide and environment setup
- [ARCHITECTURE.md](ARCHITECTURE.md) — application structure and boundaries
- [API.md](API.md) — endpoint documentation
- [DATABASE.md](DATABASE.md) — database configuration and schema strategy
- [DECISIONS.md](DECISIONS.md) — reasons behind current technical choices

## Prerequisites
- Java 25 LTS
- Maven Wrapper (included in the repo)
- PostgreSQL instance for local development

## Local environment
The project requires `JAVA_HOME` to point to the Java 25 installation.

Windows example:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4.1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

## Build commands
```powershell
./mvnw.cmd clean test
```

## Local PostgreSQL setup
The recommended local development database is PostgreSQL, started from Docker Compose:

```powershell
docker compose up -d postgres
```

Then run the application with the development profile:

```powershell
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

If you are not using Docker, set the environment variables before starting the app:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/jnvst_guru_backend"
$env:DB_USERNAME = "jnvst_guru"
$env:DB_PASSWORD = "jnvst_guru_password"
```

## Current development focus
- Verify the Java 25 build environment
- Confirm the Spring Boot application starts correctly
- Keep the application structure clean and modular
- Maintain the working Supabase JWT and authenticated-user flow
- Extend the Student Profile, state/district, and arithmetic modules without redesigning existing behavior
- Keep the Flyway schema current for the question hierarchy, MAT, language, and paper tables
- Add tests before exposing additional public APIs

## Guidance
- Do not change the working Supabase authentication flow or JWT validation layer.
- Do not broaden the public API beyond the modules already implemented.
- Keep database schema changes in Flyway and avoid editing already-applied migrations.
- Update these docs whenever a significant implementation milestone is reached.
