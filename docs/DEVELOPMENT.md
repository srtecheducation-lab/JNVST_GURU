# Development

Status: Foundation setup is in progress and verified with Java 25 + Maven Wrapper.

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
- Add local postgres configuration and a health-check contract
- Add tests before expanding domain functionality

## Guidance
- Do not add authentication or security components yet.
- Do not implement the question-bank model before the foundation is stable.
- Update these docs whenever a significant implementation milestone is reached.
