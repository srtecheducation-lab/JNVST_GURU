# Architecture

Status: Implemented foundation; question-bank modules remain planned.

## Related documentation
- [README.md](../README.md) — project overview and quick start
- [HELP.md](HELP.md) — onboarding guide and setup notes
- [API.md](API.md) — API contract and versioning
- [DATABASE.md](DATABASE.md) — persistence direction and schema planning
- [DEVELOPMENT.md](DEVELOPMENT.md) — setup and developer workflow
- [DECISIONS.md](DECISIONS.md) — architecture rationale and decision log

## Current architecture
The backend currently follows a simple Spring Boot-based modular monolith structure with a minimal, production-ready layout.

### Layers
- API layer: HTTP endpoints and REST responses
- Application layer: service logic and orchestration
- Domain layer: business concepts and validation rules
- Persistence layer: JPA entities and repositories
- Shared configuration: environment, datasource, and general Spring configuration

## Current implementation
- The app is launched from `JnvstGuruBackendApplication`.
- A health endpoint is exposed at `/api/v1/health`.
- The project is intentionally minimal and avoids premature abstractions.

## Architectural principles
- Keep modules logically separated.
- Avoid microservice or event-driven complexity at this stage.
- Use DTOs for external API responses.
- Keep database access centralized through repositories.
- Avoid over-engineering for future scale before the real requirements appear.

## Planned module boundaries
The upcoming structure may include:
- `api` for controllers and request/response DTOs
- `application` for services and use cases
- `domain` for core entities and business rules
- `infrastructure` or `persistence` for JPA repositories and technical adapters
- `config` for application configuration

## Current constraints
- No authentication or authorization implementation yet.
- No question-bank schema has been created yet.
- No event-driven or cloud-specific infrastructure is included.
